package dev.mfataka.transporter.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 25.11.2022 13:41
 */
public class TransporterLog {

    private static volatile boolean debugEnabled = false;
    private final Logger log;
    private final Logger kibana;

    public TransporterLog(Class<?> owner) {
        this.log = LoggerFactory.getLogger(owner);
        this.kibana = LoggerFactory.getLogger("kibana." + owner.getSimpleName());
    }

    public static void init(Boolean debug) {
        debugEnabled = Boolean.TRUE.equals(debug);
    }


    public void info(String message, Object... args) {
        log.info(message, args);
    }

    public void debug(String message, Object... args) {
        log.debug(message, args);
    }

    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    public void error(String message, Object... args) {
        log.error(message, args);
    }

    public void debugIfEnabled(String message, Object... args) {
        if (!debugEnabled) {
            return;
        }

        log.debug(message, args);
    }


    /**
     * sends a single record on the {@code kibana.*} logger with every marker
     * exposed as an MDC field, so a JSON encoder (logstash etc.) indexes each
     * marker as its own kibana field. MDC entries live only for the duration
     * of the emit and are always cleaned up.
     */
    public void kibanaCall(Map<KibanaMarker, String> markers, String message, Object... args) {
        markers.forEach((marker, value) -> MDC.put(marker.key(), value));
        try {
            kibana.info(message, args);
        } finally {
            markers.keySet().forEach(marker -> MDC.remove(marker.key()));
        }
    }
}
