package org.acme;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import dev.langchain4j.model.chat.ChatLanguageModel;
import java.util.HashMap;
import java.util.Map;

@Path("/hello")
public class HelloResource {

    @OpenAi
    ChatLanguageModel model;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        String prompt = "Explain in three lines how to create a song like Nirvana";
        return model.generate(prompt);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{band}")
    public String musicLike(@PathParam("band") String band) {
        String template = "Create a short song like: {{band}}";
        PromptTemplate promptTemplate = PromptTemplate.from(template);

        Map<String, Object> variables = new HashMap<>();
        variables.put("band", "band");

        Prompt prompt = promptTemplate.apply(variables);
        return model.generate(prompt.text());
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/prompt/{band}")
    public String musicLikeStructured(@PathParam("band") String band) {

        CreateSongPrompt createSongPrompt = new CreateSongPrompt(band);

        Prompt prompt = StructuredPromptProcessor.toPrompt(createSongPrompt);
        return model.generate(prompt.text());
    }
    @StructuredPrompt({
        "Create a short song like: {{band}}.",
        "Structure your answer in the following way:",

        "Title: ...",

        "Lyrics:",
        "- ...",
        "- ..."
    })
    static class CreateSongPrompt {

        String band;

        CreateSongPrompt(String band) {
            this.band = band;
        }
    }

}
