package com.fontal.fonpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.fontal.fonpicturebackend.config.CosClientConfig;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import com.fontal.fonpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Deprecated
@Service
@Slf4j
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传文件
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 文件前的目录
     * @return uploadPictureResult
     */
    public UploadPictureResult uploadPictureFile(MultipartFile multipartFile,String uploadPathPrefix){
        //1.校验文件
        ValidPicture(multipartFile);
        //2.上传图片
        //2.1.获取文件名，并重构
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //2.2.创建临时文件上传
        File file = null;
        try {
            file = File.createTempFile(uploadFilePath,null);
            multipartFile.transferTo(file);
            //2.3.上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //3.封装图片信息并返回
            //3.1.计算宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height,10).doubleValue();
            //3.2.封装并返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+'/'+uploadFilePath);
            uploadPictureResult.setPicName(originalFilename);
            uploadPictureResult.setPicSize(multipartFile.getSize());
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return  uploadPictureResult;
        } catch (Exception e) {
            log.error("上传文件出错，文件路径：{}",uploadFilePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        } finally {
            //4.删除临时文件
            deletePicFile(file);
        }
    }

    /**
     * 上传文件
     * 文件
     * @param uploadPathPrefix 文件前的目录
     * @return uploadPictureResult
     */
    public UploadPictureResult uploadPictureFileByUrl(String fileUrl,String uploadPathPrefix){
        //1.校验文件
        //ValidPicture(m);
        ValidPicture(fileUrl);
        //2.上传图片
        //2.1.获取文件名，并重构
        String uuid = RandomUtil.randomString(16);
        //String originalFilename = multipartFile.getOriginalFilename();
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        //2.2.创建临时文件上传
        File file = null;
        try {
            file = File.createTempFile(uploadFilePath,null);
            //multipartFile.transferTo(file);
            HttpUtil.downloadFile(fileUrl,file);
            //2.3.上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //3.封装图片信息并返回
            //3.1.计算宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height,10).doubleValue();
            //3.2.封装并返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+'/'+uploadFilePath);
            uploadPictureResult.setPicName(originalFilename);
            uploadPictureResult.setPicSize(file.length());
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return  uploadPictureResult;
        } catch (Exception e) {
            log.error("上传文件出错，文件路径：{}",uploadFilePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        } finally {
            //4.删除临时文件
            deletePicFile(file);
        }
    }

    private void ValidPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


    /**
     * 校验文件
     */
    public void ValidPicture(MultipartFile file) {
        //1.校验参数是否为空
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR,"上传文件为空");
        //2.限制上传图片的大小
        long fileSize = file.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 5 * ONE_M,ErrorCode.PARAMS_ERROR,"上传文件不能超过5MB");
        //3.上传文件格式是否符合白名单
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        List<String> WHITE_FORMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
        ThrowUtils.throwIf(!WHITE_FORMAT_LIST.contains(suffix),ErrorCode.PARAMS_ERROR,"不允许上传该后缀文件");
    }

    /**
     * 删除临时文件
     */
    public void deletePicFile(File file) {
        if (file == null){
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("临时文件删除失败，文件路径：{}", file.getAbsoluteFile());
        }
    }
}
