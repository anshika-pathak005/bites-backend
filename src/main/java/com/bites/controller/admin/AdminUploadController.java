package com.bites.controller.admin;

import com.bites.dto.response.ImageUploadResponse;
import com.bites.service.ImageUploadService;
import com.bites.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Admin-only endpoint for uploading images (menu items, categories, etc.)
// to Cloudinary. Returns just the URL — the frontend then submits that URL
// as part of the normal create/update request for whatever it's attached to.
@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminUploadController {

    // Service containing the upload validation and Cloudinary logic.
    private final ImageUploadService imageUploadService;

    /*
     * Uploads a single image file and returns its permanent Cloudinary URL.
     * Triggered by the "Upload Image" control on admin forms (menu items,
     * categories, etc.) before the rest of the form is submitted.
     */
    @PostMapping("/image")
    public ApiResponse<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(imageUploadService.upload(file), "Image uploaded successfully");
    }
}
