package com.outdoor.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rental.lock")
public class RentalLockProperties {

    private long waitSeconds = 3;
    private long leaseSeconds = 15;
}
