package com.enterprise.wms.dto;

import java.util.List;

public class OrderDtos {
    public record CreateOrderLineRequest(String sku, Integer quantity) {}
    public record CreateOrderRequest(String orderNo, String warehouseCode, List<CreateOrderLineRequest> lines) {}
    public record WavePlanRequest(String warehouseCode, String waveNo, List<Long> orderIds, String workerUsername) {}
}
