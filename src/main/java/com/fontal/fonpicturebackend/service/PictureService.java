package com.fontal.fonpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fontal.fonpicturebackend.model.domain.Picture;
import com.fontal.fonpicturebackend.model.domain.User;
import com.fontal.fonpicturebackend.model.dto.picture.PictureQueryRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureReviewRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadRequest;
import com.fontal.fonpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.fontal.fonpicturebackend.model.dto.user.UserQueryPage;
import com.fontal.fonpicturebackend.model.vo.picture.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 11695
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-01-26 17:40:04
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传文件
     *
     * @param inputSource url或者本地文件
     * @param pictureUploadRequest 请求包
     * @param loginUser 登入用户
     * @return vo
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片（分页）
     *
     * @param queryRequest 图片查询参数
     * @return 查询对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest queryRequest);

    /**
     * 获取PictureVo
     *
     * @param picture picture类
     * @return pictureVO
     */
    PictureVO getPictureVo(Picture picture, Long userId);

    /**
     * 将 picturePage 转为 pictureVoPage
     *
     * @param picturePage picturePage
     * @param request     req
     * @return pictureVoPage
     */
    Page<PictureVO> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request);


    /**
     * 删除图片
     *
     * @param id id
     * @return true
     */
    boolean deletePicture(Long id, HttpServletRequest request);

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest 参数
     * @param request              鉴权
     * @return true
     */
    boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request);

    /**
     * 管理员审核图片
     *
     * @param pictureReviewRequest review
     * @param loginUser            审核人
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 更新图片时，校验图片
     *
     * @param picture picture
     */
    void validPicture(Picture picture);

    /**
     * 是否时本人或者管理员
     *
     * @param request   鉴权
     * @param pictureId id
     */
    void isAuthRole(HttpServletRequest request, Long pictureId);

    /**
     * 管理员自动过审,填充字段
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片（仅管理员）
     *
     * @param request    抓取请求参数
     * @param loginUser  登录用户
     *return 图片VO列表
     */
    List<PictureVO> fetchPicturesByBatch(PictureUploadByBatchRequest request, User loginUser);
}
