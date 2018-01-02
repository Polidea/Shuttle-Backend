package com.polidea.shuttle.infrastructure;

import org.slf4j.Logger;

public class Logging {
    public static void logException(Logger logger, Exception exception) {
        logger.error("Handled exception:", exception);
    }
}
