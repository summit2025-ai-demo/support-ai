package org.parasol.customerservice.support.ai;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "langchain4j.openai.chat")
public interface LangChain4jOpenAIChatConfig {

    String baseUrl();

    String modelName();

    String apiKey();

    Optional<String> organizationId();

    @WithDefault("10s")
    Duration timeout();

    @WithDefault("1")
    Integer maxRetries();

    @WithDefault("false")
    Optional<Boolean> logRequests();

    @WithDefault("false")
    Optional<Boolean> logResponses();

    @WithDefault("0.0")
    Double temperature();
}
