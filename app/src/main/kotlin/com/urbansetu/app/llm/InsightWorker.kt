package com.urbansetu.app.llm

import android.content.Context
import androidx.work.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class InsightWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
    val ward = inputData.getString("ward") ?: "Ward 152"
    val token = inputData.getString("token") ?: ""

    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val retrofit = Retrofit.Builder()
      .baseUrl(inputData.getString("baseUrl") ?: "http://10.0.2.2:8000")
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    val api = retrofit.create(LlmClient::class.java)
    return try {
      val res = api.postInsights("Bearer $token", InsightsPayload(ward, mapOf(
        "hotspot_count" to 42,
        "avg_severity" to 0.63,
        "last_updated" to System.currentTimeMillis()
      )))
      // TODO: show a notification with res.suggestion
      Result.success()
    } catch (e: Exception) {
      Result.retry()
    }
  }

  companion object {
    fun schedule(ctx: Context, baseUrl: String, token: String, ward: String) {
      val req = PeriodicWorkRequestBuilder<InsightWorker>(java.time.Duration.ofHours(6))
        .setInputData(workDataOf("baseUrl" to baseUrl, "token" to token, "ward" to ward))
        .build()
      WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("llm-insights", ExistingPeriodicWorkPolicy.UPDATE, req)
    }
  }
}
