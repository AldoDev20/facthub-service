package com.facthub.billing.billing.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequestDto {

    @NotBlank(message = "RUC del emisor es obligatorio")
    private String rucEmisor;

    @NotBlank(message = "RUC del cliente es obligatorio")
    private String rucCliente;

    @NotEmpty(message = "La factura debe tener al menos un item")
    @Valid
    private List<ItemDto> items;
}
