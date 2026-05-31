package com.facthub.billing.billing.domain.repository;

import com.facthub.billing.billing.domain.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findBySeriesAndNumberAndDocumentType(String series, Integer number, String documentType);

    List<Invoice> findBySunatStatus(String sunatStatus);

    boolean existsBySeriesAndNumberAndDocumentType(String series, Integer number, String documentType);
}
