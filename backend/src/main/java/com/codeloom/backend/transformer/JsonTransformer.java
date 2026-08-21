package com.codeloom.backend.transformer;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
public class JsonTransformer {
    private final ObjectMapper objectMapper;

    public <T> @Nullable T fromNodeToType(JsonNode node, String name, Class<T> type) {
        JsonNode valueNode = node.get(name);
        return objectMapper.treeToValue(valueNode, type);
    }
}
