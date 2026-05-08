package com.microservice.profile_service.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String name;
    @Value("${cloudinary.api-key}")
    private String  apiKey;
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name",name,
                "api_key",apiKey,
                "api_secret",apiSecret,
                "secure",true
        ));
    }
}
