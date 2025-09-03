package dev.mfataka.transporter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HAMMA FATAKA
 */
@Data
@NoArgsConstructor
@Builder
public class DemoResponse {
    private String result;
    private String resultMessage;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DemoResponse(@JsonProperty(value = "result", required = true) String result,@JsonProperty(value = "resultMessage") String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;
    }
}
