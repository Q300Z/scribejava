package com.github.scribejava.core.services;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of {@link TimestampService} using java.time.Instant.
 */
public class TimestampServiceImpl implements TimestampService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTimestampInSeconds() {
        return String.valueOf(Instant.now().getEpochSecond());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNonce() {
        return String.valueOf(Instant.now().getEpochSecond() + ThreadLocalRandom.current().nextLong());
    }
}
