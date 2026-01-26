package com.fontal.fonpicturebackend.controller;

import com.fontal.fonpicturebackend.annotation.AuthCheck;
import com.fontal.fonpicturebackend.common.BaseResponse;
import com.fontal.fonpicturebackend.common.ResultUtils;
import com.fontal.fonpicturebackend.constant.UserConstant;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestFileController {

    @Resource
    private CosManager cosManager;


    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile multipartFile) {
        //文件目录
        String originalFilename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s",originalFilename);
        File file = null;
        try {
            file = File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("上传文件失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        } finally {
            //删除临时文件file
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("临时文件删除失败");
                }
            }
        }
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosInputStream = null;
        try {
            COSObject object = cosManager.getObject(filePath);
            cosInputStream = object.getObjectContent();
            //处理下载流
            byte[] byteArray = IOUtils.toByteArray(cosInputStream);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename"+filePath);
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("下载文件失败,文件名：{}",filePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载出错");
        } finally {
            if (cosInputStream != null) {
                cosInputStream.close();
            }
        }
    }
}
