package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.AlertType;
import com.enterprise.wms.domain.WmsEnums.MovementType;
import com.enterprise.wms.domain.entity.Alert;
import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.MovementHistory;
import com.enterprise.wms.repository.AlertRepository;
import com.enterprise.wms.repository.InventoryRepository;
import com.enterprise.wms.repository.MovementHistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final MovementHistoryRepository movementHistoryRepository;
    private final AlertRepository alertRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            MovementHistoryRepository movementHistoryRepository,
                            AlertRepository alertRepository) {
        this.inventoryRepository = inventoryRepository;
        this.movementHistoryRepository = movementHistoryRepository;
        this.alertRepository = alertRepository;
    }

    public List<Inventory> realTimeStock() {
        return realTimeStock(PageRequest.of(0, 50, Sort.by(Sort.Order.desc("id")))).getContent();
    }

    public Page<Inventory> realTimeStock(Pageable pageable) {
        return inventoryRepository.findAll(resolvePageable(pageable));
    }

    public List<MovementHistory> movementHistory() {
        return movementHistory(PageRequest.of(0, 50, Sort.by(Sort.Order.desc("id")))).getContent();
    }

    public Page<MovementHistory> movementHistory(Pageable pageable) {
        return movementHistoryRepository.findAll(resolvePageable(pageable));
    }

    @Transactional
    public Inventory cycleCountAdjust(Long inventoryId, Integer actualQty, String performedBy) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();
        int before = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int delta = actualQty - before;
        inventory.setQuantity(actualQty);
        inventoryRepository.save(inventory);

        MovementHistory adjustment = new MovementHistory();
        adjustment.setProduct(inventory.getProduct());
        adjustment.setQuantity(Math.abs(delta));
        adjustment.setMovementType(MovementType.ADJUSTMENT);
        adjustment.setEventTime(LocalDateTime.now());
        adjustment.setPerformedBy(performedBy);
        adjustment.setReferenceNo("CYCLE-COUNT");
        movementHistoryRepository.save(adjustment);
        return inventory;
    }

    @Transactional
    public List<Alert> replenishmentAlerts() {
        List<Alert> created = new ArrayList<>();
        List<Alert> existingAlerts = new ArrayList<>(alertRepository.findByAlertTypeAndResolvedFalse(AlertType.REPLENISHMENT));
        for (Inventory inventory : inventoryRepository.findAll()) {
            if (inventory.getProduct() == null) {
                continue;
            }
            int qty = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            int reorder = inventory.getProduct().getReorderLevel() == null ? 0 : inventory.getProduct().getReorderLevel();
            if (qty <= reorder) {
                String prefix = "Replenish SKU " + inventory.getProduct().getSku();
                createAlertIfAbsent(existingAlerts, AlertType.REPLENISHMENT, prefix, prefix, "HIGH").ifPresent(created::add);
            }
        }
        return created;
    }

    public Map<String, List<String>> fastSlowDeadStock() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        Map<String, Long> moveCounts = movementHistoryRepository.findByEventTimeAfter(since).stream()
                .filter(movement -> movement.getProduct() != null && movement.getProduct().getSku() != null)
                .collect(Collectors.groupingBy(movement -> movement.getProduct().getSku(), Collectors.counting()));
        List<String> sorted = moveCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();
        List<String> fast = sorted.stream().limit(5).toList();
        List<String> slow = sorted.stream().skip(Math.max(0, sorted.size() - 5)).toList();
        List<String> dead = inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getProduct() != null && inventory.getProduct().getSku() != null)
                .map(inventory -> inventory.getProduct().getSku())
                .filter(sku -> !moveCounts.containsKey(sku))
                .distinct()
                .toList();
        return Map.of("fastMoving", fast, "slowMoving", slow, "deadStock", dead);
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 50, Sort.by(Sort.Order.desc("id")));
        }
        return pageable;
    }

    private Optional<Alert> createAlertIfAbsent(List<Alert> existingAlerts,
                                                AlertType type,
                                                String messagePrefix,
                                                String message,
                                                String severity) {
        boolean exists = existingAlerts.stream()
                .map(Alert::getMessage)
                .filter(Objects::nonNull)
                .anyMatch(existingMessage -> existingMessage.startsWith(messagePrefix));
        if (exists) {
            return Optional.empty();
        }

        Alert alert = new Alert();
        alert.setAlertType(type);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        Alert savedAlert = alertRepository.save(alert);
        existingAlerts.add(savedAlert);
        return Optional.of(savedAlert);
    }
}
