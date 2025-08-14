package dev.mfataka.transporter.utils;

import org.slf4j.Logger;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 25.11.2022 13:41
 */
public record LoggingUtils(Logger logger, boolean isDebugEnabled) {

    public void debugIfEnabled(final String message, final Object... args) {
        if (isDebugEnabled) {
            logger.debug(message, args);
        }
    }
}
