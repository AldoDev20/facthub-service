package com.facthub.billing.billing.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"series", "number", "document_type"})
})
public class Invoice {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "document_type", nullable = false, length = 2)
    private String documentType;

    @Column(nullable = false, length = 4)
    private String series;

    @Column(nullable = false)
    private Integer number;

    @Column(name = "customer_ruc", nullable = false, length = 11)
    private String customerRuc;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "sunat_status", nullable = false, length = 20)
    private String sunatStatus;

    @Column(name = "sunat_ticket", length = 100)
    private String sunatTicket;

    @Column(name = "xml_file_path", length = 500)
    private String xmlFilePath;

    @Column(name = "cdr_file_path", length = 500)
    private String cdrFilePath;

    @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
        if (sunatStatus == null) {
            sunatStatus = "PENDING";
        }
    }
}
