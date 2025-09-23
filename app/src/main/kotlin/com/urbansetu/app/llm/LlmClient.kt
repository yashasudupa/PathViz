package com.urbansetu.app.llm

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LlmClient {
  @POST("/v1/insights")
  suspend fun postInsights(
    @Header("Authorization") bearer: String,
    @Body payload: InsightsPayload
  ): InsightsResponse
}

data class InsightsPayload(val ward: String, val stats: Map<String, Any>)
data class InsightsResponse(val suggestion: String, val confidence: Double)
