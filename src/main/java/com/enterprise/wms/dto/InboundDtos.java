package com.enterprise.wms.dto;

import java.time.LocalDate;

public class InboundDtos {
    public record GoodsReceiptRequest(
            String barcode,
            String warehouseCode,
            String lotNo,
            LocalDate expiryDate,
            Integer quantity,
            String grnNo,
            String performedBy
    ) {}
}
