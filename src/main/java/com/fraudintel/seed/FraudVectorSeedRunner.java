package com.fraudintel.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fraud.seed.enabled", havingValue = "true")
public class FraudVectorSeedRunner implements CommandLineRunner {

    private final SyntheticFraudCaseSeedService seedService;

    @Override
    public void run(String... args) {
        log.info("fraud.seed.enabled=true — starting vector seed job.");
        seedService.seed();
    }
}
