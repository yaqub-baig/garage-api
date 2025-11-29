package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarageInventoryItemRepository extends JpaRepository<GarageInventoryItem, Integer> {

    // Find all inventory items for a garage
    List<GarageInventoryItem> findByGarageIdAndIsActiveTrue(Integer garageId);

    // Find specific item by garage and inventory part
    Optional<GarageInventoryItem> findByGarageIdAndInventoryPartId(Integer garageId, String inventoryPartId);

    // Find low stock items
    @Query("SELECT g FROM GarageInventoryItem g WHERE g.garage.id = :garageId " +
           "AND g.isActive = true AND g.minimumStockLevel IS NOT NULL " +
           "AND g.quantityInStock <= g.minimumStockLevel")
    List<GarageInventoryItem> findLowStockItemsByGarageId(@Param("garageId") Integer garageId);

    // Find out of stock items
    @Query("SELECT g FROM GarageInventoryItem g WHERE g.garage.id = :garageId " +
           "AND g.isActive = true AND g.quantityInStock = 0")
    List<GarageInventoryItem> findOutOfStockItemsByGarageId(@Param("garageId") Integer garageId);

    // Find overstocked items
    @Query("SELECT g FROM GarageInventoryItem g WHERE g.garage.id = :garageId " +
           "AND g.isActive = true AND g.maximumStockLevel IS NOT NULL " +
           "AND g.quantityInStock >= g.maximumStockLevel")
    List<GarageInventoryItem> findOverstockedItemsByGarageId(@Param("garageId") Integer garageId);

    // Search by part name or SKU
    @Query("SELECT g FROM GarageInventoryItem g WHERE g.garage.id = :garageId " +
           "AND g.isActive = true " +
           "AND (LOWER(g.partName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.partNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<GarageInventoryItem> searchInventoryItems(@Param("garageId") Integer garageId,
                                                    @Param("searchTerm") String searchTerm);

    // Total inventory value
    @Query("SELECT SUM(g.quantityInStock * g.unitPrice) FROM GarageInventoryItem g " +
           "WHERE g.garage.id = :garageId AND g.isActive = true")
    Double calculateTotalInventoryValue(@Param("garageId") Integer garageId);

    // Count of items
    Long countByGarageIdAndIsActiveTrue(Integer garageId);
}
