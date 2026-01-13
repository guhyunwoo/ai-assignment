package bsm.insert.aiassignment.global.openai

import bsm.insert.aiassignment.domain.chat.domain.Chat
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OpenAiService(
    @Value("\${openai.api-key}")
    private val apiKey: String,

    @Value("\${openai.model}")
    private val defaultModel: String
) {
    private val openAI: OpenAI by lazy {
        OpenAI(apiKey)
    }

    fun generateResponse(question: String, history: List<Chat>, model: String?): String {
        return runBlocking {
            val messages = buildMessages(question, history)
            val request = ChatCompletionRequest(
                model = ModelId(model ?: defaultModel),
                messages = messages
            )
            val response = openAI.chatCompletion(request)
            response.choices.firstOrNull()?.message?.content ?: ""
        }
    }

    fun generateStreamingResponse(question: String, history: List<Chat>, model: String?): Flow<String> {
        val messages = buildMessages(question, history)
        val request = ChatCompletionRequest(
            model = ModelId(model ?: defaultModel),
            messages = messages
        )
        return openAI.chatCompletions(request).map { chunk ->
            chunk.choices.firstOrNull()?.delta?.content ?: ""
        }
    }

    private fun buildMessages(question: String, history: List<Chat>): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        messages.add(
            ChatMessage(
                role = ChatRole.System,
                content = "You are a helpful AI assistant."
            )
        )

        for (chat in history) {
            messages.add(
                ChatMessage(
                    role = ChatRole.User,
                    content = chat.question
                )
            )
            messages.add(
                ChatMessage(
                    role = ChatRole.Assistant,
                    content = chat.answer
                )
            )
        }

        messages.add(
            ChatMessage(
                role = ChatRole.User,
                content = question
            )
        )

        return messages
    }
}
