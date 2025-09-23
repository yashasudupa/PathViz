package com.urbansetu.app.petition

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun PetitionScreen() {
  val ctx = LocalContext.current
  var title by remember { mutableStateOf(TextFieldValue("Clean Ward 152 – Overflowing Bins")) }
  var ward by remember { mutableStateOf(TextFieldValue("Ward 152")) }
  var summary by remember { mutableStateOf(TextFieldValue("Request immediate action to add 10 bins and daily pickup on 4th Main, Jayanagar.")) }
  var target by remember { mutableStateOf(TextFieldValue("BBMP Solid Waste Mgmt")) }
  var email by remember { mutableStateOf(TextFieldValue("citizen@example.com")) }

  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Start a Petition", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(ward, { ward = it }, label = { Text("Ward/Locality") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(summary, { summary = it }, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    OutlinedTextField(target, { target = it }, label = { Text("Target authority") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(email, { email = it }, label = { Text("Your contact email") }, modifier = Modifier.fillMaxWidth())

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(onClick = {
        val body = """
          Petition: ${title.text}
          Ward: ${ward.text}
          Target: ${target.text}

          Summary:
          ${summary.text}
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_SUBJECT, "UrbanSetu Petition: ${title.text}")
          putExtra(Intent.EXTRA_TEXT, body)
        }
        ctx.startActivity(Intent.createChooser(intent, "Share Petition"))
      }) { Text("Share") }

      OutlinedButton(onClick = { /* TODO: Save locally with Room */ }) { Text("Save Draft") }
    }
  }
}
