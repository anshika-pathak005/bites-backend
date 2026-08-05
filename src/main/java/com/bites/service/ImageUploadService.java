package com.bites.service;

import com.bites.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

// Validates and uploads an admin-supplied image file (menu item photo,
// category image, etc.) to Cloudinary.
public interface ImageUploadService {

    // Rejects anything that isn't an allowed image type/size, then uploads
    // it and returns its permanent Cloudinary URL.
    ImageUploadResponse upload(MultipartFile file);
}
