package dev.mfataka.transporter.utils;

/**
 * Value of the {@link KibanaMarker#RESULT} marker.
 *
 * @author HAMMA FATAKA
 */
public enum KibanaCallResult {
    SUCCESS("success"),
    FAIL("fail"),
    TIMEOUT("timeout");

    private final String value;

    KibanaCallResult(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
