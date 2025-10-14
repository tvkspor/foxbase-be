package com.be.java.foxbase.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Upload ảnh bìa
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Ảnh bìa không được để trống!");
        }

        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "foxbase/book-cover",
                        "public_id", baseName,
                        "use_filename", true,
                        "unique_filename", false,
                        "overwrite", true
                )
        );
    }

    // Upload file PDF
    public Map<String, Object> uploadPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File PDF không được để trống!");
        }

        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());

        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "foxbase/books",
                        "public_id", baseName,
                        "use_filename", true,
                        "unique_filename", false,
                        "overwrite", true
                )
        );
    }
}
