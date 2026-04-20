package com.enterprise.wms.dto;

public class OutboundDtos {
    public record CreatePickingTaskRequest(Long orderId, String strategy, String workerUsername) {}
    public record UpdatePickingStatusRequest(String status, Integer progressPct) {}
}
