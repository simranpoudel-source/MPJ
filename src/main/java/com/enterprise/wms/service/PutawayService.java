package com.enterprise.wms.service;

import com.enterprise.wms.domain.WmsEnums.MovementType;
import com.enterprise.wms.domain.WmsEnums.VelocityClass;
import com.enterprise.wms.domain.WmsEnums.WeightClass;
import com.enterprise.wms.domain.entity.Inventory;
import com.enterprise.wms.domain.entity.Location;
import com.enterprise.wms.domain.entity.MovementHistory;
import com.enterprise.wms.domain.entity.Product;
import com.enterprise.wms.domain.entity.Warehouse;
import com.enterprise.wms.repository.InventoryRepository;
import com.enterprise.wms.repository.LocationRepository;
import com.enterprise.wms.repository.MovementHistoryRepository;
import com.enterprise.wms.repository.ProductRepository;
import com.enterprise.wms.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PutawayService {
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final MovementHistoryRepository movementHistoryRepository;
    private final ProductRepository productRepository;

    @Value("${wms.slotting.fast-zone-prefix:A}")
    private String fastZonePrefix;
    @Value("${wms.slotting.fast-zone-bonus:100}")
    private Integer fastZoneBonus;
    @Value("${wms.slotting.slow-zone-prefix:C}")
    private String slowZonePrefix;
    @Value("${wms.slotting.slow-zone-bonus:80}")
    private Integer slowZoneBonus;
    @Value("${wms.slotting.heavy-shelf-suffix:1}")
    private String heavyShelfSuffix;
    @Value("${wms.slotting.heavy-shelf-bonus:120}")
    private Integer heavyShelfBonus;
    @Value("${wms.slotting.light-shelf-suffix:3}")
    private String lightShelfSuffix;
    @Value("${wms.slotting.light-shelf-bonus:40}")
    private Integer lightShelfBonus;

    public PutawayService(WarehouseRepository warehouseRepository,
                          LocationRepository locationRepository,
                          InventoryRepository inventoryRepository,
                          MovementHistoryRepository movementHistoryRepository,
                          ProductRepository productRepository) {
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementHistoryRepository = movementHistoryRepository;
        this.productRepository = productRepository;
    }

    public Location suggestStorageLocation(String warehouseCode) {
        Warehouse warehouse = warehouseRepository.findByCode(warehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        List<Location> candidates = locationRepository.findByWarehouseAndOccupiedLessThan(warehouse, Integer.MAX_VALUE);
        return candidates.stream()
                .filter(location -> location.getCapacity() != null && location.getOccupied() != null && location.getCapacity() > location.getOccupied())
                .min(Comparator.comparingInt(location -> location.getCapacity() - location.getOccupied()))
                .orElseThrow(() -> new IllegalStateException("No available location"));
    }

    public Location suggestStorageLocation(String warehouseCode, Long productId) {
        Warehouse warehouse = warehouseRepository.findByCode(warehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        List<Location> candidates = locationRepository.findByWarehouseAndOccupiedLessThan(warehouse, Integer.MAX_VALUE);
        return candidates.stream()
                .filter(location -> location.getCapacity() != null && location.getOccupied() != null && location.getCapacity() > location.getOccupied())
                .min(Comparator.comparingInt(location -> slottingScore(location, product)))
                .orElseThrow(() -> new IllegalStateException("No slot available for product"));
    }

    @Transactional
    public Inventory executePutaway(Long inventoryId, Long locationId, Integer quantity, String performedBy) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();
        Location location = locationRepository.findById(locationId).orElseThrow();
        inventory.setLocation(location);
        inventoryRepository.save(inventory);

        location.setOccupied((location.getOccupied() == null ? 0 : location.getOccupied()) + quantity);
        locationRepository.save(location);

        MovementHistory history = new MovementHistory();
        history.setProduct(inventory.getProduct());
        history.setToLocation(location);
        history.setQuantity(quantity);
        history.setMovementType(MovementType.PUTAWAY);
        history.setEventTime(LocalDateTime.now());
        history.setPerformedBy(performedBy);
        movementHistoryRepository.save(history);
        return inventory;
    }

    private int slottingScore(Location location, Product product) {
        int score = (location.getCapacity() - location.getOccupied());
        String zone = location.getZone() == null ? "" : location.getZone().toUpperCase();
        String shelf = location.getShelf() == null ? "" : location.getShelf().toUpperCase();
        String fastPrefix = fastZonePrefix == null ? "A" : fastZonePrefix.toUpperCase();
        String slowPrefix = slowZonePrefix == null ? "C" : slowZonePrefix.toUpperCase();
        String heavySuffix = heavyShelfSuffix == null ? "1" : heavyShelfSuffix.toUpperCase();
        String lightSuffix = lightShelfSuffix == null ? "3" : lightShelfSuffix.toUpperCase();

        if (product.getVelocityClass() == VelocityClass.FAST && zone.startsWith(fastPrefix)) {
            score -= fastZoneBonus == null ? 100 : fastZoneBonus;
        }
        if (product.getVelocityClass() == VelocityClass.SLOW && zone.startsWith(slowPrefix)) {
            score -= slowZoneBonus == null ? 80 : slowZoneBonus;
        }
        if (product.getWeightClass() == WeightClass.HEAVY && shelf.endsWith(heavySuffix)) {
            score -= heavyShelfBonus == null ? 120 : heavyShelfBonus;
        }
        if (product.getWeightClass() == WeightClass.LIGHT && shelf.endsWith(lightSuffix)) {
            score -= lightShelfBonus == null ? 40 : lightShelfBonus;
        }
        return score;
    }
}
