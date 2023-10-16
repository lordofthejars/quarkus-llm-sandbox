package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/service")
public class AiServicesResource {

    @Inject
    ObjectMapper objectMapper;

    interface Assistant {
        String chat(String message);
    }

    interface Programmer {
        @SystemMessage("You are a professional developer into {{language}}")
        @UserMessage("Implement a class that {{feature}}")
        String code(@V("feature") String text, @V("language") String language);
    }

    interface BankDetailsExtractor {
        @UserMessage("Extract IBAN from {{it}}")
        String extractIBAN(String text);

        @UserMessage("Extract only the name from {{it}}")
        String extractName(String text);

        @UserMessage("Extract date from {{it}}")
        LocalDate extractTxDate(String text);

        @UserMessage("Extract amount of dollars from {{it}}")
        double extractAmount(String text);
    }

    static class TransactionInfo {

        @Description("full name")
        public String name;

        @Description("IBAN value")
        public String iban;

        @Description("Date of the transaction")
        public LocalDate transactionDate;

        @Description("Amount in dollars of the transaction")
        public double amount;

    }

    interface TransactionExtractor {
        @UserMessage("Extract information about a transaction from {{it}}")
        TransactionInfo extractTransaction(String text);
    }

    @OpenAi
    ChatLanguageModel model;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        Assistant assistant = AiServices.create(Assistant.class, model);
        return assistant.chat("Can you explain me why earth is flat?");
    }

    @GET
    @Path("/code")
    @Produces(MediaType.TEXT_PLAIN)
    public String code() {
        Programmer programmer = AiServices.create(Programmer.class, model);
        return programmer.code("encodes a string in base64", "java");
    }

    @GET
    @Path("/extract")
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode extract() {
        BankDetailsExtractor bank = AiServices.create(BankDetailsExtractor.class, model);
        String text = "My name is Alex, I did a transaction on July 4th 2023 from my account with IBAN 123456789 of $20.5";

        final ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name", bank.extractName(text))
            .put("date", bank.extractTxDate(text).format(DateTimeFormatter.ISO_DATE))
            .put("amount", bank.extractAmount(text))
            .put("IBAN", bank.extractIBAN(text));

        return objectNode;
    }

    @GET
    @Path("/transaction")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionInfo transaction() {
        TransactionExtractor bank = AiServices.create(TransactionExtractor.class, model);
        String text = "My name is Alex, I did a transaction on July 4th 2023 from my account with IBAN 123456789 of $20.5";

        return bank.extractTransaction(text);
    }

}
