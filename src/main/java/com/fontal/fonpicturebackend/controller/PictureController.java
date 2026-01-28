package com.fontal.fonpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fontal.fonpicturebackend.annotation.AuthCheck;
import com.fontal.fonpicturebackend.common.BaseResponse;
import com.fontal.fonpicturebackend.common.DeleteRequest;
import com.fontal.fonpicturebackend.common.ResultUtils;
import com.fontal.fonpicturebackend.constant.UserConstant;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import com.fontal.fonpicturebackend.model.domain.Picture;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.dto.picture.PictureQueryRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureReviewRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadRequest;
import com.fontal.fonpicturebackend.model.enums.PictureReviewStatusEnum;
import com.fontal.fonpicturebackend.model.vo.picture.PictureTagCategory;
import com.fontal.fonpicturebackend.model.vo.picture.PictureVO;
import com.fontal.fonpicturebackend.service.PictureService;
import com.fontal.fonpicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.currentUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片（可重新上传）
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.currentUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 删除图片
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = pictureService.deletePicture(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 更新图片
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        boolean result = pictureService.updatePicture(pictureUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取图片（管理员权限）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);

        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片封装
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVoById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);

        Picture picture = pictureService.getById(id);

        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureService.getPictureVo(picture, userService.currentUser(request).getId()));
    }

    /**
     * 获取pictureVo 分页
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVoPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        //鉴权,普通用户只能看到审核通过的图片
        User loginUser = userService.currentUser(request);
        if (!userService.isAdmin(loginUser)) {
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(pictureService.getPictureVoPage(picturePage, request));
    }

    /**
     * 获取标签和分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.currentUser(request);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取图片（仅管理员）
     */
    @PostMapping("/fetch/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<PictureVO>> fetchPicturesByBatch(@RequestBody PictureUploadByBatchRequest request,
                                                              HttpServletRequest req) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.currentUser(req);
        List<PictureVO> pictureVOList = pictureService.fetchPicturesByBatch(request, loginUser);
        return ResultUtils.success(pictureVOList);
    }

}
