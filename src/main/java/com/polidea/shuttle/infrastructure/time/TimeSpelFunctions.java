package com.polidea.shuttle.infrastructure.time;

public interface TimeSpelFunctions {

    static long periodToMillis(String period) {
        return PeriodConverter.periodToMillis(period);
    }
}
