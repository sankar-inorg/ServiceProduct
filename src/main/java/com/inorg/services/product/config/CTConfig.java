package com.inorg.services.product.config;

import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties("app.commercetools")
@Data
public class CTConfig {
    private String projectKey;
    private String clientId;
    private String clientSecret;

    @Bean
    public com.commercetools.api.client.ProjectApiRoot ctApiClient() {
        return ApiRootBuilder.of()
                .defaultClient(ClientCredentials.of()
                                .withClientId(clientId)
                                .withClientSecret(clientSecret)
                                .build(),
                        ServiceRegion.AWS_US_EAST_2)
                .build(projectKey);
    }
}