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
    /** Description of the item or service */
    private String description;

    /** Quantity of the item */
    private BigDecimal quantity;

    /** Unit price of the item */
    private BigDecimal unitPrice;
}
