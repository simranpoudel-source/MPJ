package com.enterprise.wms.dto;

public class LlmDtos {
    public record NaturalLanguageQuery(String text) {}
    public record ActionCommand(String action, Integer days, String sku, String warehouseCode) {}
    public record AlertResolveResponse(Long id, Boolean resolved, String message) {}
}
