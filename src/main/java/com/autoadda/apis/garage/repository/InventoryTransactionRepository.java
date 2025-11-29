package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.InventoryTransaction;
import com.autoadda.apis.garage.util.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    // Find all transactions for a specific inventory item
    List<InventoryTransaction> findByGarageInventoryItemIdOrderByTransactionDateDesc(Integer itemId);

    // Find transactions for a garage (through inventory items)
    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.garageInventoryItem.garage.id = :garageId " +
           "ORDER BY t.transactionDate DESC")
    Page<InventoryTransaction> findByGarageId(@Param("garageId") Integer garageId, Pageable pageable);

    // Find transactions by type
    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.garageInventoryItem.garage.id = :garageId " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findByGarageIdAndTransactionType(
            @Param("garageId") Integer garageId,
            @Param("transactionType") TransactionType transactionType);

    // Find transactions by date range
    @Query("SELECT t FROM InventoryTransaction t " +
           "WHERE t.garageInventoryItem.garage.id = :garageId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findByGarageIdAndDateRange(
            @Param("garageId") Integer garageId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find transactions by reference
    List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Integer referenceId);
}
