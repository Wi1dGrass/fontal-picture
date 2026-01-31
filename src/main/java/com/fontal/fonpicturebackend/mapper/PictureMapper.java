package com.fontal.fonpicturebackend.mapper;

import com.fontal.fonpicturebackend.model.domain.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 11695
 * @description 针对表【picture(图片)】的数据库操作Mapper
 * @createDate 2026-01-26 17:40:04
 * @Entity model.domain.Picture
 */
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 按审核状态统计图片数量
     *
     * @return List<Map < String, Object>> 每个Map包含 reviewStatus 和 count
     */
    @Select("SELECT reviewStatus, COUNT(*) as count FROM picture WHERE isDelete = 0 GROUP BY reviewStatus")
    List<Map<String, Object>> countByReviewStatus();

}




