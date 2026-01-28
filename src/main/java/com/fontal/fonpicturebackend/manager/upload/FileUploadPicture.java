package com.fontal.fonpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class FileUploadPicture extends PictureUploadTemplate{
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        //1.校验参数是否为空
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR,"上传文件为空");
        //2.限制上传图片的大小
        long fileSize = file.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 5 * ONE_M,ErrorCode.PARAMS_ERROR,"上传文件不能超过5MB");
        //3.上传文件格式是否符合白名单
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        List<String> WHITE_FORMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
        ThrowUtils.throwIf(!WHITE_FORMAT_LIST.contains(suffix),ErrorCode.PARAMS_ERROR,"不允许上传该后缀文件");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
