package com.peekchat.ai

import com.peekchat.model.AnalysisReport
import com.peekchat.model.Conversation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * DeepSeek API Provider（Android 端实现）。
 * 使用 Ktor HttpClient，兼容 OpenAI API 格式。
 */
class DeepSeekProvider(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.deepseek.com"
) : AiProvider {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyze(conversation: Conversation): Result<AnalysisReport> {
        return try {
            val prompt = PromptBuilder.build(conversation)

            val response = httpClient.post("$baseUrl/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                setBody(ChatCompletionRequest(
                    model = "deepseek-chat",
                    messages = listOf(
                        Message("system", "你是一个聊天记录分析助手。"),
                        Message("user", prompt)
                    )
                ))
                // TODO: 添加 Authorization header (Bearer $apiKey)
            }

            val completion: ChatCompletionResponse = response.body()
            val content = completion.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from AI"))

            // 解析 JSON 响应中的 report 部分
            val report = json.decodeFromString<AnalysisReport>(content)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// OpenAI-compatible request/response DTOs
@kotlinx.serialization.Serializable
private data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>
)

@kotlinx.serialization.Serializable
private data class Message(
    val role: String,
    val content: String
)

@kotlinx.serialization.Serializable
private data class ChatCompletionResponse(
    val choices: List<Choice>
)

@kotlinx.serialization.Serializable
private data class Choice(
    val message: Message
)
