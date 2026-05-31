package com.facthub.billing.billing.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
}
