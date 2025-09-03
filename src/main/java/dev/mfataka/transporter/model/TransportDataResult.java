package dev.mfataka.transporter.model;

/**
 * @author HAMMA FATAKA
 */
public enum TransportDataResult {
    OK,
    FAIL,
    UNKNOWN;

    public boolean isOk() {
        return this == OK;
    }

    public boolean isFail() {
        return !isOk();
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

}
