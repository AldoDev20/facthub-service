package com.facthub.billing.directory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Taxpayer {
    private String ruc;
    private String nombre;
    private String estado;
    private String condicionDomicilio;

    public boolean isActive() {
        return "ACTIVO".equalsIgnoreCase(estado) && "HABIDO".equalsIgnoreCase(condicionDomicilio);
    }
}
