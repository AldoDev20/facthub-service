package com.facthub.billing.billing.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequence {

    @Id
    @Column(length = 4)
    private String series;

    @Column(name = "last_number", nullable = false)
    private Integer lastNumber;
}
