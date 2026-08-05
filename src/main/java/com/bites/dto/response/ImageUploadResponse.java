package com.bites.dto.response;

// Returned after successfully uploading an image — the frontend drops this
// URL into the imageUrl field of whatever form triggered the upload.
public record ImageUploadResponse(String url) {
}
