package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService {

    @Override
    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;

        log.info("开始上传文件，上传文件的文件名：{}，上传路径：{}，新文件名：{}", fileName, path, uploadFileName);

        // 动态创建上传文件目录，目录位于WEB-INF中
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            // IMPORTANT：设置写权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try {
            // 将file写入targetFile，即上传文件
            file.transferTo(targetFile);
            // 将targetFile上传至FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 上传完成后，删除upload目录下面的文件
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();
    }
}
