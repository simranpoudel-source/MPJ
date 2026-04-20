package com.enterprise.wms.service;

import com.enterprise.wms.dto.LlmDtos.ActionCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OpenAiActionService {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wms.openai.api-key}")
    private String apiKey;

    @Value("${wms.openai.model}")
    private String model;

    public ActionCommand parseAction(String input) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackRuleBased(input);
        }
        try {
            String prompt = """
                    Convert warehouse query to JSON with fields:
                    action, days, sku, warehouseCode.
                    If missing values, keep null.
                    Query: %s
                    """.formatted(input);

            String body = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", model)
                            .set("messages", objectMapper.createArrayNode()
                                    .add(objectMapper.createObjectNode().put("role", "system").put("content", "You are a WMS intent parser. Return JSON only."))
                                    .add(objectMapper.createObjectNode().put("role", "user").put("content", prompt)))
            );
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return fallbackRuleBased(input);
                }
                JsonNode root = objectMapper.readTree(response.body().string());
                String content = root.path("choices").get(0).path("message").path("content").asText("{}");
                JsonNode json = objectMapper.readTree(content);
                return new ActionCommand(
                        json.path("action").asText("UNKNOWN"),
                        json.path("days").isNumber() ? json.path("days").asInt() : null,
                        json.path("sku").isTextual() ? json.path("sku").asText() : null,
                        json.path("warehouseCode").isTextual() ? json.path("warehouseCode").asText() : null
                );
            }
        } catch (IOException e) {
            return fallbackRuleBased(input);
        }
    }

    private ActionCommand fallbackRuleBased(String input) {
        String text = input.toLowerCase();
        if (text.contains("expiring") || text.contains("expiry") || text.contains("expire")) {
            int days = 30;
            if (text.contains("7")) days = 7;
            else if (text.contains("14") || text.contains("two week")) days = 14;
            else if (text.contains("60") || text.contains("two month")) days = 60;
            else if (text.contains("90")) days = 90;
            return new ActionCommand("GET_EXPIRING_PRODUCTS", days, null, null);
        }
        if (text.contains("low stock") || text.contains("reorder") || text.contains("below")) {
            return new ActionCommand("GET_LOW_STOCK", null, null, null);
        }
        if (text.contains("warehouse") && (text.contains("status") || text.contains("overview") || text.contains("summary"))) {
            return new ActionCommand("GET_WAREHOUSE_STATUS", null, null, null);
        }
        if (text.contains("movement") || text.contains("activity") || text.contains("recent")) {
            return new ActionCommand("GET_MOVEMENTS", null, null, null);
        }
        if (text.contains("alert") || text.contains("warning") || text.contains("critical")) {
            return new ActionCommand("GET_ALERTS", null, null, null);
        }
        if (text.contains("inventory") || text.contains("stock") || text.contains("sku")) {
            return new ActionCommand("GET_INVENTORY_SUMMARY", null, null, null);
        }
        if (text.contains("order") || text.contains("picking") || text.contains("outbound")) {
            return new ActionCommand("GET_ORDERS", null, null, null);
        }
        return new ActionCommand("UNKNOWN", null, null, null);
    }
}
