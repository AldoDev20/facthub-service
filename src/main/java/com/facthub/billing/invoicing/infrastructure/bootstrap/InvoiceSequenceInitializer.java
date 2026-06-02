package com.facthub.billing.invoicing.infrastructure.bootstrap;

import com.facthub.billing.invoicing.domain.model.InvoiceSequence;
import com.facthub.billing.invoicing.domain.repository.InvoiceSequenceRepository;
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

        for (String series : requiredSeries) {
            if (!sequenceRepository.existsById(series)) {
                System.out.println("Initializing series in the database: " + series);
                InvoiceSequence sequence = new InvoiceSequence();
                sequence.setSeries(series);
                sequence.setLastNumber(0);
                sequenceRepository.save(sequence);
            }
        }
    }
}
