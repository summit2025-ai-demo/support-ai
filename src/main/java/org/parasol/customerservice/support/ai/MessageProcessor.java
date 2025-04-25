package org.parasol.customerservice.support.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    @Inject
    LangChain4jOpenAIChatConfig langChain4jOpenAIChatConfig;

    @Inject
    LangChain4jOpenAIEmbeddingConfig langChain4jOpenAIEmbeddingConfig;

    @Inject
    MilvusConfig milvusConfig;

    @ConfigProperty(name = "embedding.max-results")
    int embeddingMaxResults;

    @ConfigProperty(name = "embedding.min-score")
    double embeddingMinScore;

    ChatLanguageModel chatLanguageModel;

    OpenAiEmbeddingModel embeddingModel;

    MilvusEmbeddingStore embeddingStore;

    ContentRetriever contentRetriever;

    Assistant assistant;

    void onStart(@Observes StartupEvent e) {
        chatLanguageModel = initChatLanguageModel();
        embeddingModel = initEmbeddingModel();
        embeddingStore = initEmbeddingStore();
        contentRetriever = initContentRetriever();
        assistant = initAiService();
    }

    public String process(String query) {
        return assistant.answer(query);
    }

    private ChatLanguageModel initChatLanguageModel() {
        LOGGER.info("Initializing chat model");
        return OpenAiChatModel.builder()
                .apiKey(langChain4jOpenAIChatConfig.apiKey())
                .modelName(langChain4jOpenAIChatConfig.modelName())
                .baseUrl(langChain4jOpenAIChatConfig.baseUrl())
                .logRequests(langChain4jOpenAIChatConfig.logRequests().orElse(false))
                .logResponses(langChain4jOpenAIChatConfig.logResponses().orElse(false))
                .timeout(langChain4jOpenAIChatConfig.timeout())
                .temperature(langChain4jOpenAIChatConfig.temperature())
                .build();
    }

    private OpenAiEmbeddingModel initEmbeddingModel() {
        LOGGER.info("Initializing embedding model");
        OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder builder =
                OpenAiEmbeddingModel.builder()
                        .baseUrl(langChain4jOpenAIEmbeddingConfig.baseUrl())
                        .modelName(langChain4jOpenAIEmbeddingConfig.modelName())
                        .apiKey(langChain4jOpenAIEmbeddingConfig.apiKey())
                        .timeout(langChain4jOpenAIEmbeddingConfig.timeout())
                        .maxRetries(langChain4jOpenAIEmbeddingConfig.maxRetries())
                        .logRequests(langChain4jOpenAIEmbeddingConfig.logRequests().orElse(false))
                        .logResponses(langChain4jOpenAIEmbeddingConfig.logResponses().orElse(false));
        if (langChain4jOpenAIEmbeddingConfig.setDimension()) {
            builder.dimensions(langChain4jOpenAIEmbeddingConfig.dimension());
        }
        return builder.build();
    }

    private MilvusEmbeddingStore initEmbeddingStore() {
        LOGGER.info("Initializing embedding store");
        return MilvusEmbeddingStore.builder()
                .uri(milvusConfig.uri())
                .username(milvusConfig.username())
                .password(milvusConfig.password())
                .collectionName(milvusConfig.collectionName())
                .dimension(milvusConfig.dimension())
                .build();
    }

    private ContentRetriever initContentRetriever() {
        LOGGER.info("Initializing content retriever");
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(embeddingMaxResults)
                .minScore(embeddingMinScore)
                .build();
    }

    private Assistant initAiService() {
        LOGGER.info("Initializing AiService");
        return AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}
