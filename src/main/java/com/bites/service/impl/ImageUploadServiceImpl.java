package com.bites.service.impl;

import com.bites.dto.response.ImageUploadResponse;
import com.bites.exception.BadRequestException;
import com.bites.service.ImageUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {

    // Only these are accepted — matches "JPG, JPEG, PNG, WEBP" agreed with the admin UI.
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp");

    // Matches the multipart.max-file-size already set in application.yml —
    // checked again here so a rejected upload gets a clear message instead
    // of a raw multipart exception.
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    // Cloudinary client bean, configured in CloudinaryConfig.
    private final Cloudinary cloudinary;

    @Override
    public ImageUploadResponse upload(MultipartFile file) {
        // Step 1:
        // Reject empty submissions and anything that isn't an allowed image
        // type/size before spending a Cloudinary API call on it.
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was uploaded");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPG, JPEG, PNG, and WEBP images are allowed");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Image must be 5MB or smaller");
        }

        // Step 2:
        // Upload the raw bytes to Cloudinary and hand back its permanent,
        // secure URL — that's the only thing ever stored in our own database.
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "bites",
                    "resource_type", "image"
            ));
            return new ImageUploadResponse((String) result.get("secure_url"));
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }
}
