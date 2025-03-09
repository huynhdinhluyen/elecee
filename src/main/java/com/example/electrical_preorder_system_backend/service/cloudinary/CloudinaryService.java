package com.example.electrical_preorder_system_backend.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CloudinaryService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5 MB.");
        }
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(file.getContentType())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and GIF allowed.");
        }
    }

    @Async
    public CompletableFuture<String> uploadFileAsync(MultipartFile file) {
        validateFile(file);
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded: {}", secureUrl);
            return CompletableFuture.completedFuture(secureUrl);
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new IllegalArgumentException("Cloudinary upload failed", e);
        }
    }

    @Async
    public CompletableFuture<Boolean> deleteImageAsync(String imageUrl) {
        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                log.warn("Could not extract public ID from URL: {}", imageUrl);
                return CompletableFuture.completedFuture(false);
            }

            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");
            boolean success = "ok".equals(status);

            if (success) {
                log.info("Successfully deleted image from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete image from Cloudinary: {}, status: {}", publicId, status);
            }

            return CompletableFuture.completedFuture(success);
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", imageUrl, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // Use URI instead of the deprecated URL constructor
            URI uri = URI.create(imageUrl);
            URL url = uri.toURL();
            String path = url.getPath();

            // Rest of the existing extraction logic
            String[] segments = path.split("/");
            if (segments.length < 5) {
                return null;
            }

            StringBuilder publicId = new StringBuilder();
            for (int i = 4; i < segments.length; i++) {
                publicId.append(segments[i]);
                if (i < segments.length - 1) {
                    publicId.append("/");
                }
            }

            // Remove file extension if present
            String result = publicId.toString();
            int lastDotPos = result.lastIndexOf(".");
            if (lastDotPos > 0) {
                result = result.substring(0, lastDotPos);
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", imageUrl, e);
            return null;
        }
    }
}