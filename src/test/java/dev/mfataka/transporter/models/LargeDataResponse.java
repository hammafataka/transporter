package dev.mfataka.transporter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 31.05.2023 14:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LargeDataResponse {
    private List<LargeData> largeDataList;
}
