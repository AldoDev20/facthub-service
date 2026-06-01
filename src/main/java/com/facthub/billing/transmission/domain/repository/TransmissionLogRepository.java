package com.facthub.billing.transmission.domain.repository;

import com.facthub.billing.invoicing.domain.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransmissionLogRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findBySunatStatusAndSunatTicketIsNull(String sunatStatus);
}
