package com.fontal.fonpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.fontal.fonpicturebackend.config.CosClientConfig;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.manager.CosManager;
import com.fontal.fonpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix){
        // 校验文件（本地 / url）
        validPicture(inputSource);
        // 重构文件名
        String uuid = RandomUtil.randomString(16);
        // 获取原文件名（本地 / url）
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadFilePath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        // 创建临时文件上传
        File file = null;
        try {
            file = File.createTempFile(uploadFilePath,null);
            //处理文件（本地 / url）
            processFile(inputSource,file);
            // 上传文件COS
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadFilePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            return getUploadPictureResult(uploadFilePath, originalFilename, file, imageInfo);
        } catch (Exception e) {
            log.error("上传文件出错，文件路径：{}",uploadFilePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        } finally {
            //4.删除临时文件
            deletePicFile(file);
        }
    }


    /**
     * 校验文件
     * @param inputSource 文件或者url
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取文件名字
     * @param inputSource 文件或者url
     * @return 文件名字
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件
     *
     * @param inputSource 文件或者url
     * @param file file
     */
    protected abstract void processFile(Object inputSource,File file) throws IOException;

    /**
     * 封装返回结果
     */
    private UploadPictureResult getUploadPictureResult(String uploadFilePath, String originalFilename,
                                                       File file,ImageInfo imageInfo) {
        //计算宽高比
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double picScale = NumberUtil.round(width * 1.0 / height,10).doubleValue();
        //3.2.封装并返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadFilePath);
        uploadPictureResult.setPicName(originalFilename);
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
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
