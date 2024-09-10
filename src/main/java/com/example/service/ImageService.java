package com.example.service;

import java.io.IOException;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Nruonan
 * @description
 */
public interface ImageService {
    String uploadAvatar(MultipartFile file, int id) throws IOException;

    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;
}
