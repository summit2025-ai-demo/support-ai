package org.parasol.customerservice.support.ai;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

@StaticInitSafe
@ConfigMapping(prefix = "langchain4j.openai.embedding")
public interface LangChain4jOpenAIEmbeddingConfig {

    String baseUrl();

    String modelName();

    String apiKey();

    Optional<String> organizationId();

    @WithDefault("768")
    Integer dimension();

    @WithDefault("False")
    Boolean setDimension();

    @WithDefault("10s")
    Duration timeout();

    @WithDefault("1")
    Integer maxRetries();

    @WithDefault("false")
    Optional<Boolean> logRequests();

    @WithDefault("false")
    Optional<Boolean> logResponses();
}
