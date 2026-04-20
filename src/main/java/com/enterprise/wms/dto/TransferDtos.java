package com.enterprise.wms.dto;

public class TransferDtos {
    public record TransferRequest(String fromWarehouseCode, String toWarehouseCode, String sku, Integer quantity, String performedBy) {}
}
