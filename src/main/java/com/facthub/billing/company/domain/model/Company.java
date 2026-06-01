package com.facthub.billing.company.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(nullable = false, length = 255)
    private String businessName;

    @Column(nullable = false, length = 100)
    private String sunatSolUsername;

    @Column(nullable = false, length = 100)
    private String sunatSolPassword;

    @Column(nullable = false)
    private byte[] certificateContent;

    @Column(nullable = false, length = 100)
    private String certificatePassword;
}
