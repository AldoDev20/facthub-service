package com.facthub.billing.transmission.infrastructure.sunat;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class XSenderSunatServiceTest {

    @Mock
    private CamelContext camelContext;

    @InjectMocks
    private XSenderSunatService xSenderSunatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void serviceInstantiatedWithMocks() {
        assertNotNull(xSenderSunatService, "XSenderSunatService should be instantiated");
        assertNotNull(camelContext, "CamelContext mock should be injected");
        
        // Ensure producer template can be mocked
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        when(camelContext.createProducerTemplate()).thenReturn(producerTemplate);
        
        assertNotNull(camelContext.createProducerTemplate(), "Should return a mock ProducerTemplate");
    }
}
