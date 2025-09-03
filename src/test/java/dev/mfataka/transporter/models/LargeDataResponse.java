package dev.mfataka.transporter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HAMMA FATAKA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LargeDataResponse {
    private List<LargeData> largeDataList;
}
