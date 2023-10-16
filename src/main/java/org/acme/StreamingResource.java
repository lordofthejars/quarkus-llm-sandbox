package org.acme;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.data.message.UserMessage.userMessage;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/stream")
public class StreamingResource {

    @StreamingOpenAi
    StreamingChatLanguageModel streamModel;

    @OpenAi
    ChatLanguageModel model;

    @Channel("response")
    Multi<String> response;

    @Channel("response")
    Emitter<String> emitter;

    @GET
    @Path("/")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> stream() {
        return response;
    }

    @GET
    @Path("/memory")
    @Produces(MediaType.TEXT_PLAIN)
    public void memory() {

        Tokenizer tokenizer = new OpenAiTokenizer(GPT_3_5_TURBO);
        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, tokenizer);

        /**SystemMessage systemMessage = SystemMessage.from(
            "You are a senior developer explaining to another senior Java developer " +
                  "using developing in Quarkus framework");
        chatMemory.add(systemMessage);**/

        UserMessage userMessage1 = userMessage(
            "How to write a REST endpoint in Java? ");
        chatMemory.add(userMessage1);

        emitter.send("[User]: " + userMessage1.text());
        final Response<AiMessage> response1 = model.generate(chatMemory.messages());
        chatMemory.add(response1.content());
        emitter.send("[LLM]: "+ response1.content().text());

        UserMessage userMessage2 = userMessage(
            "Create a test of the first point? " +
                "Be short, 15 lines of code maximum.");
        chatMemory.add(userMessage2);

        emitter.send("[User]: " + userMessage2.text());

        final Response<AiMessage> response2 = model.generate(chatMemory.messages());

        emitter.send("[LLM]: " + response2.content().text());

    }

    @GET
    @Path("/chain")
    @Produces(MediaType.TEXT_PLAIN)
    public void chain() {
        ConversationalChain chain = ConversationalChain.builder()
            .chatLanguageModel(model)
            .build();

        String userMessage1 = "Can you give a brief explanation of Kubernetes, 3 lines max?";
        emitter.send("[User]: " + userMessage1);

        String answer1 = chain.execute(userMessage1);
        emitter.send("[LLM]: " + answer1);

        String userMessage2 = "Can you give me a YAML example to deploy an application for that?";
        emitter.send("[User]: " + userMessage2);

        String answer2 = chain.execute(userMessage2);
        emitter.send("[LLM]: " + answer2);

    }

    @GET
    @Path("/ask")
    @Produces(MediaType.TEXT_PLAIN)
    public void hello() {
        String prompt = "Explain me why earth is flat";
        streamModel.generate(prompt, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                emitter.send(token);
            }

            @Override
            public void onError(Throwable error) {
                Log.error(error.getMessage());
            }
        });

    }

}
