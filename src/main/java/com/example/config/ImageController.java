package com.example.config;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Nruonan
 * @description
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Resource
    ImageService service;
    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("file") MultipartFile file,
        @RequestAttribute(Const.ATTR_USER_ID) int id) throws IOException {
        if(file.getSize() > 1024 * 100)
            return RestBean.failure(400, "头像图片不能大于100KB");
        log.info("正在进行头像上传操作...");
        String url = service.uploadAvatar(file, id);
        if(url != null) {
            log.info("头像上传成功，大小: " + file.getSize());
            return RestBean.success(url);
        } else {
            return RestBean.failure(400, "头像上传失败，请联系管理员！");
        }
    }
}
