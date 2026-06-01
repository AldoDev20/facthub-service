package com.facthub.billing.invoicing.domain.repository;

import com.facthub.billing.invoicing.domain.model.InvoiceSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, String> {

    @Query("SELECT s FROM InvoiceSequence s WHERE s.series = :series")
    InvoiceSequence findBySeriesForUpdate(String series);

    @Modifying
    @Transactional
    @Query("UPDATE InvoiceSequence s SET s.lastNumber = s.lastNumber + 1 WHERE s.series = :series")
    int incrementAndGetNumber(String series);
}
