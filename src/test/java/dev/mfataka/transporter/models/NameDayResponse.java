package dev.mfataka.transporter.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 15.05.2023 11:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameDayResponse {
    private int day;
    private int month;
    private NameDay nameDay;
    private String country;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameDay {
        private String cz;
    }
}
