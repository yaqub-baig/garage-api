package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Integer> {

    // Get all line items for a given invoice
    List<InvoiceLineItem> findByInvoiceId(Integer invoiceId);
}
