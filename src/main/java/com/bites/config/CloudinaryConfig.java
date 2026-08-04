package com.bites.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Only image URLs are persisted in the database — Cloudinary is the source
 * of truth for the actual files. See util/ImageUploadService (added alongside
 * the menu-management module).
 */
@Configuration
public class CloudinaryConfig {

    // Name of the Cloudinary account/cloud used to store menu item images.
    @Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    // API key used to authenticate with Cloudinary.
    @Value("${app.cloudinary.api-key}")
    private String apiKey;

    // API secret used to authenticate with Cloudinary. Keep this private/secure.
    @Value("${app.cloudinary.api-secret}")
    private String apiSecret;

    /*
     * Builds the Cloudinary client bean, configured with the account credentials.
     * Other parts of the app (like image upload) inject this bean to upload
     * and manage images.
     */
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
