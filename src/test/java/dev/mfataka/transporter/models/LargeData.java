package dev.mfataka.transporter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 31.05.2023 14:12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LargeData {
    private String firstField;
    private String secondField;
}
