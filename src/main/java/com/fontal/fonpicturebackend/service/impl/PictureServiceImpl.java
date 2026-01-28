package com.fontal.fonpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import com.fontal.fonpicturebackend.exception.ThrowUtils;
import com.fontal.fonpicturebackend.manager.fetch.BingImageFetcher;
import com.fontal.fonpicturebackend.manager.upload.FileUploadPicture;
import com.fontal.fonpicturebackend.manager.upload.PictureUploadTemplate;
import com.fontal.fonpicturebackend.manager.upload.UrlUploadPicture;
import com.fontal.fonpicturebackend.model.domain.Picture;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.dto.file.UploadPictureResult;
import com.fontal.fonpicturebackend.model.dto.picture.PictureQueryRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureReviewRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadRequest;
import com.fontal.fonpicturebackend.model.enums.PictureReviewStatusEnum;
import com.fontal.fonpicturebackend.model.vo.picture.PictureVO;
import com.fontal.fonpicturebackend.model.vo.user.UserVo;
import com.fontal.fonpicturebackend.service.PictureService;
import com.fontal.fonpicturebackend.mapper.PictureMapper;
import com.fontal.fonpicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 11695
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2026-01-26 17:40:04
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private BingImageFetcher bingImageFetcher;

    @Resource
    private FileUploadPicture fileUploadPicture;

    @Resource
    private UrlUploadPicture uploadPictureFile;
    @Resource
    private UrlUploadPicture urlUploadPicture;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(inputSource == null,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //2.判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        //3.上传文件
        //构建文件前缀“public/{userId}”
        String uploadPathPre = String.format("public/%s", loginUser.getId());
        PictureUploadTemplate pictureUploadTemplate = fileUploadPicture;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlUploadPicture;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPre);

        //4. 封装PictureVO对象
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //填充审核字段
        this.fillReviewParams(picture,loginUser);
        //5.存入数据库中
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return Picture.objToVO(picture);
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                /*like "\" + tage "\"("\Java\")*/
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVo(Picture picture, Long userId) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            pictureVO.setUser(userService.userToUserVO(user));
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        //将pictureList 转为 pictureVoList
        List<PictureVO> pictureVOList = pictureList.stream().map(Picture::objToVO).toList();
        //搜集userId对象,并且去重
        List<Long> userIdList = pictureList.stream()
                .map(Picture::getUserId)
                .filter(ObjUtil::isNotEmpty)
                .distinct()
                .toList();
        //查询user信息 Map(Long,UserVo)
        if (CollUtil.isNotEmpty(userIdList)) {
            List<User> userList = userService.listByIds(userIdList);

            Map<Long, UserVo> userIdtoUserVoMap = userList.stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            user -> userService.userToUserVO(user),
                            (v1, v2) -> v1
                    ));

            // 封装PictureVo
            pictureVOList.forEach(pictureVO -> {
                Long userId = pictureVO.getUserId();
                if (userId != null) {
                    pictureVO.setUser(userIdtoUserVoMap.get(userId));
                }
            });
        }

        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }


    @Override
    public boolean deletePicture(Long id, HttpServletRequest request) {
        //1.查看图片是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);

        //2.校验权限，仅限用户本人，和管理员删除
        isAuthRole(request, picture.getId());

        //3.删除图片
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }


    @Override
    public boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        //1.校验参数
        Long pictureId = pictureUpdateRequest.getId();
        ThrowUtils.throwIf(pictureId == null || pictureId < 0, ErrorCode.PARAMS_ERROR, "图片id不为空");
        //2.校验权限
        User loginUser = userService.currentUser(request);
        isAuthRole(request, pictureId);
        //3.封装picture对象
        Picture oldPicture = this.getById(pictureId);
        validPicture(oldPicture);

        //更新时新封装一个类
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        picture.setEditTime(new Date());
        this.fillReviewParams(picture,loginUser);
        //4.更新
        boolean update = this.updateById(picture);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewMessage == null || reviewStatus.equals(PictureReviewStatusEnum.REVIEWING.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!picture.getReviewStatus().equals(reviewStatus),
                ErrorCode.PARAMS_ERROR,"请勿重复审核");
        //开始封装
        Picture newPicture = new Picture();
        newPicture.setEditTime(new Date());
        BeanUtils.copyProperties(pictureReviewRequest,newPicture);
        boolean result = this.updateById(newPicture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void validPicture(Picture picture) {
        //校验参数
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片id不为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 1024, ErrorCode.PARAMS_ERROR, "图片简介过长");
        }
    }

    @Override
    public void isAuthRole(HttpServletRequest request, Long pictureId) {
        Long userId = this.getById(pictureId).getUserId();
        User loginUser = userService.currentUser(request);
        Long loginUserId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isCurrentUser = loginUserId.equals(userId);
        ThrowUtils.throwIf(!isAdmin && !isCurrentUser, ErrorCode.NO_AUTH_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public List<PictureVO> fetchPicturesByBatch(PictureUploadByBatchRequest request, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅管理员可批量抓取图片");

        String searchText = request.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索词不能为空");

        Integer count = request.getCount();
        if (count == null || count <= 0) {
            count = 10;
        }
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "单次抓取数量不能超过30");

        // 2. 从 Bing 抓取图片信息
        log.info("管理员 {} 开始批量抓取图片，搜索词: {}, 数量: {}", loginUser.getId(), searchText, count);
        List<BingImageFetcher.ImageInfo> imageInfoList = bingImageFetcher.fetchImages(searchText, count);
        ThrowUtils.throwIf(CollUtil.isEmpty(imageInfoList), ErrorCode.OPERATION_ERROR, "未抓取到有效图片");

        // 3. 批量上传图片
        List<PictureVO> result = new ArrayList<>();
        String uploadPathPre = String.format("public/%s", loginUser.getId());
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < imageInfoList.size(); i++) {
            BingImageFetcher.ImageInfo imageInfo = imageInfoList.get(i);
            try {
                // 构建带扩展名的 URL（添加 # 后缀，不影响实际下载）
                String urlWithExt = imageInfo.getUrl() + "#." + imageInfo.getExtension().replace(".", "");

                // 通过 URL 上传图片
                UploadPictureResult uploadPictureResult = urlUploadPicture.uploadPicture(urlWithExt, uploadPathPre);

                // 构建图片名称：前缀_序号
                String pictureName = buildPictureName(request.getPreFixName(), searchText, i + 1);

                // 封装 Picture 对象
                Picture picture = new Picture();
                picture.setUrl(uploadPictureResult.getUrl());
                picture.setName(pictureName);
                picture.setPicSize(uploadPictureResult.getPicSize());
                picture.setPicWidth(uploadPictureResult.getPicWidth());
                picture.setPicHeight(uploadPictureResult.getPicHeight());
                picture.setPicScale(uploadPictureResult.getPicScale());
                picture.setPicFormat(uploadPictureResult.getPicFormat());
                picture.setUserId(loginUser.getId());

                // 填充分类和标签
                picture.setCategory(StrUtil.isNotBlank(request.getCategory()) ? request.getCategory() : searchText);
                if (CollUtil.isNotEmpty(request.getTags())) {
                    picture.setTags(JSONUtil.toJsonStr(request.getTags()));
                } else {
                    // 默认将搜索词作为标签
                    List<String> defaultTags = List.of(searchText);
                    picture.setTags(JSONUtil.toJsonStr(defaultTags));
                }

                // 填充简介
                if (StrUtil.isNotBlank(request.getIntroduction())) {
                    picture.setIntroduction(request.getIntroduction());
                } else if (StrUtil.isNotBlank(imageInfo.getDescription())) {
                    picture.setIntroduction(imageInfo.getDescription());
                }

                // 填充审核字段（管理员自动过审）
                fillReviewParams(picture, loginUser);

                // 保存到数据库
                boolean saveResult = this.save(picture);
                if (saveResult) {
                    result.add(Picture.objToVO(picture));
                    successCount++;
                } else {
                    failCount++;
                }

            } catch (Exception e) {
                failCount++;
                log.warn("上传第 {} 张图片失败: {}", i + 1, e.getMessage());
            }
        }

        log.info("批量抓取完成，成功: {}, 失败: {}", successCount, failCount);
        ThrowUtils.throwIf(CollUtil.isEmpty(result), ErrorCode.OPERATION_ERROR, "所有图片上传失败");
        return result;
    }

    /**
     * 构建图片名称：前缀_序号
     */
    private String buildPictureName(String preFixName, String searchText, int index) {
        String prefix = StrUtil.isNotBlank(preFixName) ? preFixName : searchText;
        String suffix = String.format("%03d", index);
        return String.format("%s_%s", prefix, suffix);
    }

}




