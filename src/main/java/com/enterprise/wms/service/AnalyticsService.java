package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.AlertType;
import com.enterprise.wms.domain.entity.Alert;
import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.LotBatch;
import com.enterprise.wms.repository.AlertRepository;
import com.enterprise.wms.repository.InventoryRepository;
import com.enterprise.wms.repository.LotBatchRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AnalyticsService {
    private final InventoryRepository inventoryRepository;
    private final LotBatchRepository lotBatchRepository;
    private final AlertRepository alertRepository;

    public AnalyticsService(InventoryRepository inventoryRepository,
                            LotBatchRepository lotBatchRepository,
                            AlertRepository alertRepository) {
        this.inventoryRepository = inventoryRepository;
        this.lotBatchRepository = lotBatchRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional
    public List<Alert> runRuleEngine() {
        List<Alert> created = new ArrayList<>();
        List<Alert> lowStockAlerts = new ArrayList<>(alertRepository.findByAlertTypeAndResolvedFalse(AlertType.LOW_STOCK));
        List<Alert> expiryAlerts = new ArrayList<>(alertRepository.findByAlertTypeAndResolvedFalse(AlertType.EXPIRY));

        for (Inventory inventory : inventoryRepository.findAll()) {
            if (inventory.getProduct() == null) {
                continue;
            }
            int qty = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            int reorder = inventory.getProduct().getReorderLevel() == null ? 0 : inventory.getProduct().getReorderLevel();
            if (qty <= reorder) {
                String prefix = "Low stock: " + inventory.getProduct().getSku();
                createAlertIfAbsent(lowStockAlerts, AlertType.LOW_STOCK, prefix, prefix, "HIGH").ifPresent(created::add);
            }
        }
        for (LotBatch lot : lotBatchRepository.findByExpiryDateLessThanEqual(LocalDate.now().plusDays(7))) {
            String prefix = "Expiry soon for lot " + lot.getLotNo();
            String message = prefix + (lot.getProduct() != null ? " / SKU " + lot.getProduct().getSku() : "");
            createAlertIfAbsent(expiryAlerts, AlertType.EXPIRY, prefix, message, "MEDIUM").ifPresent(created::add);
        }
        return created;
    }

    public List<Alert> activeAlerts() {
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }

    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void scheduledRuleCheck() {
        runRuleEngine();
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
