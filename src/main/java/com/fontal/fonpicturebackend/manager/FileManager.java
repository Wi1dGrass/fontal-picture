package com.fontal.fonpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
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
import java.util.*;

@Service
@Slf4j
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    public UploadPictureResult uploadPictureFile(MultipartFile multipartFile,String uploadPathPrefix){
        //1.校验文件
        ValidPicture(multipartFile);
        //2.上传图片
        //2.1.获取文件名，并重构
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilePath = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
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

    public void ValidPicture(MultipartFile file) {
        //1.校验参数是否为空
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR,"上传文件为空");
        //2.限制上传图片的大小
        long fileSize = file.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 5 * ONE_M,ErrorCode.PARAMS_ERROR,"上传文件不能超过5MB");
        //3.上传文件格式是否符合白名单
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        List<String> WHITE_FORMAT_LIST = Arrays.asList("png","jpg","jpeg","git","webp");
        ThrowUtils.throwIf(!WHITE_FORMAT_LIST.contains(suffix),ErrorCode.PARAMS_ERROR,"不允许上传该后缀文件");
    }

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
