package com.polidea.shuttle.infrastructure.time;

import static java.lang.String.format;

// TODO implement milliseconds extraction.
// TODO IMO, DON'T implement years and months extraction (those are of unstable length).
class PeriodConverter {
    private static final long MINUTE = 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = 24 * HOUR;

    private static final String PERIOD_REGEX = "((\\d)+[dhsmDHSM])+";

    static long periodToMillis(String period) {
        validateFormat(period);
        period = period.toLowerCase();

        int hours = extractQuantity(period, "h");
        int minutes = extractQuantity(period, "m");
        int seconds = extractQuantity(period, "s");
        int days = extractQuantity(period, "d");

        return ((days * DAY) + (hours * HOUR) + (minutes * MINUTE) + (seconds)) * 1000;
    }

    private static void validateFormat(String period) {
        if (!period.matches(PERIOD_REGEX)) {
            throw new IllegalArgumentException(format("%s is not a period that can be parsed.", period));
        }
    }

    private static int extractQuantity(String period, String what) {
        return period.contains(what) ? getTimeUnitQuantity(period, what) : 0;
    }

    private static int getTimeUnitQuantity(String period, String timeUnitSymbol) {
        int lastDigitIndex = period.indexOf(timeUnitSymbol) - 1;
        int firstDigitIndex = findFirstDigitIndex(period, lastDigitIndex);

        return firstDigitIndex == lastDigitIndex ? Character.getNumericValue(period.charAt(firstDigitIndex))
            : Integer.parseInt(period.substring(firstDigitIndex, lastDigitIndex + 1));
    }

    private static int findFirstDigitIndex(String period, int lastDigitIndex) {
        int firstDigitIndex = lastDigitIndex;

        for (int i = lastDigitIndex; i >= 0; i--) {
            if (!Character.isDigit(period.charAt(i)) && i != 0) {
                break;
            } else {
                firstDigitIndex = i;
            }
        }
        return firstDigitIndex;
    }
}
