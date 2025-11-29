package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.InventoryAlert;
import com.autoadda.apis.garage.util.AlertStatus;
import com.autoadda.apis.garage.util.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Integer> {

    // Find all active alerts for a garage
    @Query("SELECT a FROM InventoryAlert a " +
           "WHERE a.garageInventoryItem.garage.id = :garageId " +
           "AND a.alertStatus = 'ACTIVE' " +
           "ORDER BY a.createdAt DESC")
    List<InventoryAlert> findActiveAlertsByGarageId(@Param("garageId") Integer garageId);

    // Find active alerts by type
    @Query("SELECT a FROM InventoryAlert a " +
           "WHERE a.garageInventoryItem.garage.id = :garageId " +
           "AND a.alertStatus = 'ACTIVE' " +
           "AND a.alertType = :alertType " +
           "ORDER BY a.createdAt DESC")
    List<InventoryAlert> findActiveAlertsByGarageIdAndType(
            @Param("garageId") Integer garageId,
            @Param("alertType") AlertType alertType);

    // Find existing active alert for an item
    Optional<InventoryAlert> findByGarageInventoryItemIdAndAlertTypeAndAlertStatus(
            Integer itemId, AlertType alertType, AlertStatus alertStatus);

    // Count active alerts for a garage
    @Query("SELECT COUNT(a) FROM InventoryAlert a " +
           "WHERE a.garageInventoryItem.garage.id = :garageId " +
           "AND a.alertStatus = 'ACTIVE'")
    Long countActiveAlertsByGarageId(@Param("garageId") Integer garageId);

    // Get all alerts for an inventory item
    List<InventoryAlert> findByGarageInventoryItemIdOrderByCreatedAtDesc(Integer itemId);
}
