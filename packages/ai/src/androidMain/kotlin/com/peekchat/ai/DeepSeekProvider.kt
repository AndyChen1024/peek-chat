package com.peekchat.ai

import com.peekchat.model.AnalysisReport
import com.peekchat.model.Conversation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * DeepSeek V4 Flash API Provider（Android Ktor 实现）。
 *
 * 三层 JSON 防护（per earlier decision）：
 * 1. response_format: json_object — constrains API to return JSON
 * 2. BEGIN_JSON / END_JSON markers — isolate JSON from thinking-mode free text
 * 3. kotlinx.serialization parse → fallback to error AnalysisReport
 */
class DeepSeekProvider(
    private val apiKey: String,
    private val baseUrl: String = "https://api.deepseek.com/v1"
) : AiProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    override suspend fun analyze(conversation: Conversation): Result<AnalysisReport> {
        return try {
            val prompt = PromptBuilder.build(conversation)

            val response = httpClient.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(ChatCompletionRequest(
                    model = "deepseek-chat",
                    messages = listOf(
                        Message("system", "你是一个聊天记录分析助手。请只输出 JSON，不要包含其他内容。"),
                        Message("user", prompt)
                    ),
                    response_format = ResponseFormat(type = "json_object")
                ))
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                return Result.failure(Exception("DeepSeek API error: ${response.status}, body=$errorBody"))
            }

            val completion: ChatCompletionResponse = response.body()
            val rawContent = completion.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI"))

            // BEGIN_JSON/END_JSON extraction: strip anything outside the markers
            val jsonContent = extractJson(rawContent)

            val report = try {
                json.decodeFromString<AnalysisReport>(jsonContent)
            } catch (e: Exception) {
                // Parse failure → return an error report so the UI doesn't crash
                AnalysisReport(
                    summary = "AI 分析解析失败: ${e.message}",
                    todos = emptyList(),
                    sentiment = com.peekchat.model.Sentiment(
                        overall = "未知",
                        positive = emptyList(),
                        negative = emptyList()
                    ),
                    decisions = emptyList()
                )
            }

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── JSON extraction ────────────────────────────────────────────

    /**
     * Extract JSON content from AI response.
     *
     * Strategy:
     * 1. If BEGIN_JSON/END_JSON markers exist, take the content between them
     * 2. Otherwise try to find the first { ... } block (raw JSON)
     * 3. Fallback: return the full content (let the parser try)
     */
    private fun extractJson(raw: String): String {
        val beginMarker = "BEGIN_JSON"
        val endMarker = "END_JSON"

        val beginIdx = raw.indexOf(beginMarker)
        val endIdx = raw.indexOf(endMarker)

        if (beginIdx != -1 && endIdx != -1 && endIdx > beginIdx) {
            return raw.substring(beginIdx + beginMarker.length, endIdx).trim()
        }

        // No markers: find the first { } block
        val braceStart = raw.indexOf('{')
        val braceEnd = raw.lastIndexOf('}')
        if (braceStart != -1 && braceEnd != -1 && braceEnd > braceStart) {
            return raw.substring(braceStart, braceEnd + 1).trim()
        }

        return raw.trim()
    }
}

// ── OpenAI-compatible DTOs ──────────────────────────────────────────

@Serializable
private data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val response_format: ResponseFormat? = null
)

@Serializable
private data class ResponseFormat(
    val type: String
)

@Serializable
private data class Message(
    val role: String,
    val content: String
)

@Serializable
private data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
private data class Choice(
    val message: Message
)
