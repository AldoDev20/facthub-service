package com.facthub.billing.directory.infrastructure.searchpe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.facthub.billing.directory.infrastructure.dto.ContribuyenteDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class SearchpeClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public SearchpeClient(
            @Value("${searchpe.api.url:https://searchpe-atelier.onrender.com}") String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public ContribuyenteDto obtenerContribuyentePorRuc(String ruc) {
        try {
            String url = baseUrl + "/api/contribuyentes/" + ruc;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), ContribuyenteDto.class);
            } else if (response.statusCode() == 404) {
                return null;
            } else {
                throw new RuntimeException("Error al consultar Searchpe: HTTP " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while calling Searchpe API", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar Searchpe API", e);
        }
    }
}
