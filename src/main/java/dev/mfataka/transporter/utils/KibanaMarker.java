package dev.mfataka.transporter.utils;

/**
 * Markers attached to the per-call kibana log record. {@link #key()} is the
 * stable field name indexed in kibana — do not rename values without
 * coordinating with the kibana dashboards.
 *
 * @author HAMMA FATAKA
 */
public enum KibanaMarker {
    OPERATOR("operator"),
    ENDPOINT("endpoint"),
    STATUS("http_status"),
    RESULT("result"),
    CORRELATION_ID("correlation_id"),
    DURATION("duration_s");

    private final String key;

    KibanaMarker(final String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
