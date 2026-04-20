package com.enterprise.wms.domain;

public class WmsEnums {
    public enum LocationType { STORAGE, PICK_FACE, STAGING, RECEIVING, SHIPPING }
    public enum MovementType { RECEIVE, PUTAWAY, PICK, REPLENISH, ADJUSTMENT, SHIP, TRANSFER_IN, TRANSFER_OUT }
    public enum OrderStatus { CREATED, PARTIALLY_ALLOCATED, ALLOCATED, PICKED, PACKED, SHIPPED }
    public enum PickingStrategy { SINGLE, BATCH, WAVE }
    public enum PickingTaskStatus { CREATED, IN_PROGRESS, COMPLETED }
    public enum AlertType { LOW_STOCK, EXPIRY, DEAD_STOCK, REPLENISHMENT }
    public enum RoleName { ADMIN, MANAGER, WORKER }
    public enum VelocityClass { FAST, MEDIUM, SLOW }
    public enum WeightClass { LIGHT, MEDIUM, HEAVY }
}
