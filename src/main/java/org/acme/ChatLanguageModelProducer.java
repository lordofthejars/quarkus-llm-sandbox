package org.acme;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;


@ApplicationScoped
public class ChatLanguageModelProducer {

    @ConfigProperty(name = "openai.key")
    String key;

    @Produces
    @StreamingOpenAi
    StreamingChatLanguageModel createStreaming() {
        return OpenAiStreamingChatModel
                        .builder()
                        .apiKey(key)
                        .modelName(GPT_3_5_TURBO)
                        .timeout(Duration.ofSeconds(60))
                        .build();
    }

    @Produces
    @OpenAi
    ChatLanguageModel create() {
        return OpenAiChatModel.builder()
            .apiKey(key)
            .modelName(GPT_3_5_TURBO)
            .timeout(Duration.ofSeconds(30))
            .build();
    }
}
