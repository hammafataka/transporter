package dev.mfataka.transporter.model;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 25.07.2022 12:50
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
