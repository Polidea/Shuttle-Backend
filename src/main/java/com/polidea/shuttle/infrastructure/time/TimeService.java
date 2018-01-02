package com.polidea.shuttle.infrastructure.time;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class TimeService {

    public Instant currentTime() {
        return Clock.systemUTC().instant();
    }

}
