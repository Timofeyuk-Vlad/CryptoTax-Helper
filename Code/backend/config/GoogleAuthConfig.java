package com.cryptotax.helper.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GoogleAuthConfig {

    @Bean
    public GoogleAuthenticator gAuth() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                .setWindowSize(5)
                .setNumberOfScratchCodes(0)
                .build();

        return new GoogleAuthenticator(config);
    }
}