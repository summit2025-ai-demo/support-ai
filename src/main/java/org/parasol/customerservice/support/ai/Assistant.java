package org.parasol.customerservice.support.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {

    @SystemMessage("""
            You are a helpful assistant answering questions based on the provided context.
            """)
    @UserMessage("""
            Provide an answer to the user query.
            {{it}}
            """)
    String answer(String query);
}
