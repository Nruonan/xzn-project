package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.ImageDO;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Nruonan
 * @description
 */
public interface ImageService extends IService<ImageDO> {
    String uploadAvatar(MultipartFile file, int id) throws IOException;

    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;

    String uploadImage(MultipartFile file, int id) throws IOException;
}
