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

    @NotBlank(message = "Tipo de comprobante es obligatorio (FACTURA o BOLETA)")
    private String tipoComprobante;

    @NotBlank(message = "Tipo de documento del cliente es obligatorio (RUC, DNI, CE)")
    private String tipoDocumentoCliente;

    @NotBlank(message = "Número de documento del cliente es obligatorio")
    private String numeroDocumentoCliente;

    private String nombreCliente; // Obligatorio para BOLETA (DNI/CE), Opcional para FACTURA (se busca en Searchpe)

    @NotEmpty(message = "La factura debe tener al menos un item")
    @Valid
    private List<ItemDto> items;
}
