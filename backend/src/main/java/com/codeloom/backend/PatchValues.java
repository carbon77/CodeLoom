package com.codeloom.backend;

import tools.jackson.databind.*;

public final class PatchValues {
  private PatchValues() {}

  public static <T> T value(
      JsonNode node, String name, ObjectMapper mapper, T current, Class<T> type) {
    JsonNode v = node.get(name);
    return v == null || v.isNull() ? current : mapper.treeToValue(v, type);
  }
}
