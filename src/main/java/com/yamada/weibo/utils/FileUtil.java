package com.yamada.weibo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class FileUtil {

    public static String imageHost;

    public static String imagePath;

    @Value("${upload.imageHost}")
    public void setImageHost(String imageHost) {
        FileUtil.imageHost = imageHost;
    }

    @Value("${upload.imagePath}")
    public void setImagePath(String imagePath) {
        FileUtil.imagePath = imagePath;
    }

    public static boolean upload(MultipartFile file, String path, String fileName) {
        String realPath = path + "/" + fileName;
        File f = new File(realPath);
        //判断文件父目录是否存在
        if (!f.getParentFile().exists()){
            if (!f.getParentFile().mkdir()) {
                log.error("【上传图片】创建目录错误: " + f.toString());
                return false;
            }
        }
        try {
            file.transferTo(f);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(String path, String fileName) {
        String fullPath = path + "/" + fileName;
        File file = new File(fullPath);
        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                log.error("【删除文件】删除文件失败：" + fullPath);
                return false;
            }
        }
        log.error("【删除文件】文件不存在：" + fullPath);
        return false;
    }
}
