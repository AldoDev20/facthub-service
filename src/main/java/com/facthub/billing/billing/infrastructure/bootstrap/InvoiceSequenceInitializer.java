package com.facthub.billing.billing.infrastructure.bootstrap;

import com.facthub.billing.billing.domain.model.InvoiceSequence;
import com.facthub.billing.billing.domain.repository.InvoiceSequenceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceSequenceInitializer implements CommandLineRunner {

    private final InvoiceSequenceRepository sequenceRepository;

    public InvoiceSequenceInitializer(InvoiceSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> requiredSeries = List.of("F001", "B001");

        for (String serie : requiredSeries) {
            if (!sequenceRepository.existsById(serie)) {
                System.out.println("Inicializando serie en la base de datos: " + serie);
                InvoiceSequence sequence = new InvoiceSequence();
                sequence.setSeries(serie);
                sequence.setLastNumber(0);
                sequenceRepository.save(sequence);
            }
        }
    }
}
