package dev.mfataka.transporter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;

import dev.mfataka.transporter.BaseTransporter;
import dev.mfataka.transporter.DefaultBaseTransporter;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 25.03.2025 12:18
 */
@Configuration
@AutoConfigureOrder
@AutoConfigureAfter(ValidationAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TransporterAutoConfiguration {
    private final Environment environment;

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
