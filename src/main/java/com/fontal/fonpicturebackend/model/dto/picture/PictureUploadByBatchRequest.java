package com.fontal.fonpicturebackend.model.dto.picture;

import lombok.Data;

import java.util.List;

@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 图片前缀
     */
    private String preFixName;

    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 简介
     */
    private String introduction;
}
