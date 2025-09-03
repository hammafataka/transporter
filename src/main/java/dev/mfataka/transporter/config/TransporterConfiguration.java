package dev.mfataka.transporter.config;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HAMMA FATAKA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransporterConfiguration {
    private boolean proxyEnabled;
    private boolean followRedirection;
    private boolean timeoutEnabled;
    private boolean resolverEnabled;
    private Boolean debugEnabled;
    private Boolean mtlsEnabled;
    private Boolean sslEnabled;
    private Boolean loggerEnabled;
    private Boolean logAuthHeaderEnabled;
    private String resolver;
    private String baseUrl;
    private String trustStorePath;
    private String trustStorePass;
    private String keystorePath;
    private String keystorePass;
    private String proxyAddress;
    @Builder.Default
    private Integer proxyPort = 0;
    @Builder.Default
    private Long timeout = 0L;
    private TimeUnit timeUnit;
    private boolean checkRequiredFields;
    private boolean requiredNonNull;
    private Boolean trustAll;
    private Integer dataLimit;
    private String alias;
    private String certPath;
}
