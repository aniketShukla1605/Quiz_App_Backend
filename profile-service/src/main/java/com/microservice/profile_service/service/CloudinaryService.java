package com.microservice.profile_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadAvatar(MultipartFile file, String userId) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", "avatars/" + userId,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    public void deleteAvatar(String userId) {
        try {
            cloudinary.uploader().destroy(
                    "avatars/" + userId,
                    ObjectUtils.emptyMap()
            );
        } catch (IOException e) {
            throw new RuntimeException("Image deletion failed: " + e.getMessage());
        }
    }
}
