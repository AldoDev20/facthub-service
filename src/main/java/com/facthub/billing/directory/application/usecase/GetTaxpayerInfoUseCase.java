package com.facthub.billing.directory.application.usecase;

import com.facthub.billing.directory.domain.model.Taxpayer;
import com.facthub.billing.directory.infrastructure.dto.ContribuyenteDto;
import com.facthub.billing.directory.infrastructure.searchpe.SearchpeClient;
import org.springframework.stereotype.Service;

@Service
public class GetTaxpayerInfoUseCase {

    private final SearchpeClient searchpeClient;

    public GetTaxpayerInfoUseCase(SearchpeClient searchpeClient) {
        this.searchpeClient = searchpeClient;
    }

    /**
     * Retrieves taxpayer information from Searchpe API by RUC.
     *
     * @param ruc the taxpayer RUC number
     * @return Taxpayer domain object
     * @throws RuntimeException if RUC is not found or invalid
     */
    public Taxpayer execute(String ruc) {
        ContribuyenteDto contribuyente = searchpeClient.obtenerContribuyentePorRuc(ruc);

        if (contribuyente == null || contribuyente.getNombre() == null) {
            throw new RuntimeException("RUC no válido o no encontrado: " + ruc);
        }

        return Taxpayer.builder()
                .ruc(contribuyente.getRuc())
                .nombre(contribuyente.getNombre())
                .estado(contribuyente.getEstado())
                .condicionDomicilio(contribuyente.getCondicionDomicilio())
                .build();
    }
}
