package dev.mfataka.transporter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import dev.mfataka.transporter.BaseTransporter;
import dev.mfataka.transporter.DefaultBaseTransporter;
import dev.mfataka.transporter.utils.TransporterLog;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 25.03.2025 12:18
 */
@Configuration
@AutoConfigureOrder
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration",
        "org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration"
})
public class TransporterAutoConfiguration {
    private final Environment environment;

    @Autowired
    public TransporterAutoConfiguration(final Environment environment, final ConfigurationResolver resolver) {
        this.environment = environment;
        TransporterLog.init(resolver.isDebugEnabled());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigurationResolver buildConfigurationResolver() {
        return new ConfigurationResolverImp(environment);
    }


    @Bean
    @ConditionalOnMissingBean
    public BaseTransporter baseTransporter(final ConfigurationResolver configurationResolver) {
        return new DefaultBaseTransporter(configurationResolver);
    }
}
