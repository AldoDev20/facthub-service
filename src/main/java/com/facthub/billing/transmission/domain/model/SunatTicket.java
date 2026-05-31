package com.facthub.billing.transmission.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SunatTicket {
    private String ticket;
    private String status;
    private String description;
    private byte[] cdrContent;
    private String cdrFileName;

    public boolean isAccepted() {
        return "ACCEPTED".equalsIgnoreCase(status);
    }
}
