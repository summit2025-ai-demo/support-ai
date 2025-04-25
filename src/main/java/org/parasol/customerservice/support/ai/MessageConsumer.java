package org.parasol.customerservice.support.ai;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.reactive.messaging.kafka.Record;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

@ApplicationScoped
public class MessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);

    @Inject
    ErrorEventEmitter errorEventEmitter;

    @Inject
    OutflowEmitter outflowEmitter;

    @Inject
    MessageProcessor messageProcessor;

    @Incoming("support")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public Uni<Void> consume(Record<String, String> message) {
        LOGGER.info("Received message:{}", message.value());
        return Uni.createFrom().item(message).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().invoke(m -> {
                    JsonObject json = new JsonObject(m.value());
                    String content = json.getString("content");
                    String reason = json.getJsonObject("structured").getString("reason");
                    String query = buildQuery(content, reason);
                    String answer = messageProcessor.process(query);
                    LOGGER.info("Received answer starting with '{}'", answer.substring(0, 100));
                    json.put("support", answer);
                    outflowEmitter.emit(m.key(), json.encode());
                })
                .onItem().transformToUni(m -> Uni.createFrom().voidItem())
                .onFailure().recoverWithItem(t -> {
                    LOGGER.error("Error while processing Message", t);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    t.printStackTrace(pw);
                    if (t instanceof DecodeException) {
                        handleError(message.key(), new JsonObject(), t.getMessage(), sw.toString(), message.value());
                    } else {
                        handleError(message.key(), new JsonObject(message.value()), t.getMessage(), sw.toString(), null);
                    }
                    return null;
                });
    }

    private String buildQuery(String content, String context) {
        StringBuilder query = new StringBuilder(content);
        if (context != null && !context.isEmpty()) {
            String contextPrompt = """
                    
                    Additional context:
                    %s
                    """.formatted(context);
            query.append(contextPrompt);
        }
        return query.toString();
    }

    private void handleError(String key, JsonObject json, String errorMessage, String stacktrace, String message) {
        LOGGER.error("Error while processing message: {}", errorMessage);
        JsonObject error = new JsonObject();
        error.put("source", "router");
        error.put("error", errorMessage);
        error.put("stack_trace", stacktrace);
        if (message == null) {
            JsonArray errors = json.getJsonArray("errors");
            if (errors == null) {
                errors = new JsonArray();
            }
            errors.add(error);
            json.put("errors", errors);
            errorEventEmitter.emit(key, json.encode());
        } else {
            error.put("message", message);
            errorEventEmitter.emit(key, error.encode());
        }
    }
}
