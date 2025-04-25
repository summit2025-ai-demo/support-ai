package org.parasol.customerservice.support.ai;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "milvus")
public interface MilvusConfig {

    String uri();

    String username();

    String password();

    Integer dimension();

    String collectionName();
}
