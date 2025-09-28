import os
import uuid
import json
import base64
import aiofiles
import asyncio
import httpx
from concurrent.futures import ThreadPoolExecutor
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import openai
from typing import Optional

openai.api_key = os.getenv("OPENAI_API_KEY")
EXECUTOR = ThreadPoolExecutor(max_workers=2)

app = FastAPI()
UPLOAD_DIR = "./static"
os.makedirs(UPLOAD_DIR, exist_ok=True)
app.mount("/static", StaticFiles(directory=UPLOAD_DIR), name="static")

@app.post("/api/analyze-upload")
async def analyze_upload(
    image: UploadFile = File(...),
    latitude: Optional[str] = Form(None),
    longitude: Optional[str] = Form(None),
    userId: Optional[str] = Form(None),
):
    # 1) Save uploaded file locally
    tmp_name = os.path.join(UPLOAD_DIR, f"{uuid.uuid4()}.jpg")
    async with aiofiles.open(tmp_name, "wb") as f:
        await f.write(await image.read())

    host = os.getenv("PUBLIC_HOST", "http://127.0.0.1:8000")
    public_url = f"{host}/static/{os.path.basename(tmp_name)}"

    # 2) Ask model for analysis (text -> JSON)
    try:
        analysis_prompt = (
            "You are an assistant that analyzes urban street photos. "
            "Given the image at the URL, return ONLY a JSON object with the keys:\n"
            "- litter_count (int)\n"
            "- litter_area_ratio (float 0..1)\n"
            "- haze_score (float 0..1)\n"
            "- vehicles (object like {\"car\":int, \"auto\":int, \"bike\":int})\n"
            "- overlay_instructions: list of {label:string, bbox:{x:float,y:float,w:float,h:float}} where bbox coords are normalized 0..1\n"
            "- reward: {points_total:int, breakdown:[{name,points,reason}], confidence:float, explanation:string}\n\n"
            f"Image URL: {public_url}\n\nReturn only valid JSON parsable by standard JSON parsers."
        )

        # NOTE: adjust model name to the chat/vision model you have access to.
        comp = openai.ChatCompletion.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are a strict JSON-output assistant."},
                {"role": "user", "content": analysis_prompt},
            ],
            temperature=0.0,
            max_tokens=800,
        )

        text_out = comp.choices[0].message["content"].strip()
        analysis_json = json.loads(text_out)
    except Exception as e:
        print("Model analysis failed:", e)
        analysis_json = {
            "litter_count": 0,
            "litter_area_ratio": 0.0,
            "haze_score": 0.0,
            "vehicles": {},
            "overlay_instructions": [],
            "reward": {"points_total": 0, "breakdown": [], "confidence": 0.0, "explanation": "fallback"}
        }

    # 3) Ask OpenAI Image Edit endpoint to 'beautify' the image (async)
    annotated_name = None
    annotated_url = public_url      # fallback to original
    annotated_b64 = None
    try:
        beautify_prompt = (
            "Beautify and clean this urban street photo for awareness presentation: "
            "remove visible litter and small trash items while keeping the scene realistic, "
            "improve color saturation and contrast, reduce haze, and place a subtle translucent "
            "label 'Cleaned' at the top-left corner. Do not remove important objects like people or cars. "
            "Return a natural, photorealistic image."
        )

        # read bytes off-thread to avoid blocking event loop
        def read_file_bytes():
            with open(tmp_name, "rb") as f:
                return f.read()
        img_bytes = await asyncio.get_event_loop().run_in_executor(EXECUTOR, read_file_bytes)

        # build multipart and call OpenAI Images Edits API with httpx (async)
        headers = {"Authorization": f"Bearer {openai.api_key}"}
        multipart = httpx.MultipartData(
            [
                ("image[]", ("input.jpg", img_bytes, "image/jpeg")),
                ("model", (None, "gpt-image-1")),
                ("prompt", (None, beautify_prompt)),
                ("size", (None, "1024x1024")),
                ("n", (None, "1")),
            ]
        )

        async with httpx.AsyncClient(timeout=120.0) as client:
            resp = await client.post("https://api.openai.com/v1/images/edits", headers=headers, content=multipart)
            resp.raise_for_status()
            resp_json = resp.json()

        # OpenAI may return data[0].b64_json or data[0].url
        edited_bytes = None
        if "data" in resp_json and len(resp_json["data"]) > 0:
            entry = resp_json["data"][0]
            if entry.get("b64_json"):
                edited_bytes = base64.b64decode(entry["b64_json"])
            elif entry.get("url"):
                # fetch the url
                async with httpx.AsyncClient() as client:
                    r2 = await client.get(entry["url"])
                    r2.raise_for_status()
                    edited_bytes = r2.content

        if not edited_bytes:
            raise RuntimeError("No edited image returned")

        # save edited image locally
        annotated_name = os.path.join(UPLOAD_DIR, f"ann_{uuid.uuid4()}.jpg")
        async with aiofiles.open(annotated_name, "wb") as af:
            await af.write(edited_bytes)

        annotated_url = f"{host}/static/{os.path.basename(annotated_name)}"
        annotated_b64 = base64.b64encode(edited_bytes).decode("utf-8")

    except Exception as ex:
        print("Image-edit call failed:", ex)
        # annotated_url remains public_url; annotated_b64 remains None

    # 4) Build response JSON, include both URL and base64 (if available)
    resp_obj = {
        "annotatedImageUrl": annotated_url,
        "annotatedImageB64": annotated_b64,   # may be None if edit failed
        "analysis": {
            "litter_count": analysis_json.get("litter_count", 0),
            "litter_area_ratio": analysis_json.get("litter_area_ratio", 0.0),
            "haze_score": analysis_json.get("haze_score", 0.0),
            "vehicles": analysis_json.get("vehicles", {}),
            "overlay_instructions": analysis_json.get("overlay_instructions", []),
        },
        "reward": analysis_json.get("reward", {"points_total": 0, "breakdown": [], "confidence": 0.0, "explanation": ""})
    }

    return JSONResponse(resp_obj)
