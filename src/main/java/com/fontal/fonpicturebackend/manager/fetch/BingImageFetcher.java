package com.fontal.fonpicturebackend.manager.fetch;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fontal.fonpicturebackend.exception.BusinessException;
import com.fontal.fonpicturebackend.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Bing 图片抓取器
 * 从 Bing 图片搜索结果中解析图片信息
 */
@Slf4j
@Component
public class BingImageFetcher {

    private static final String BING_CN_IMAGE_URL = "https://cn.bing.com/images/async";

    private static final int DEFAULT_COUNT = 10;

    private static final int MAX_COUNT = 30;

    /**
     * 抓取图片 URL 列表
     *
     * @param searchText 搜索词
     * @param count      抓取数量
     * @return 图片 URL 列表
     */
    public List<String> fetchImageUrls(String searchText, Integer count) {
        List<ImageInfo> infos = fetchImages(searchText, count);
        List<String> urls = new ArrayList<>();
        for (ImageInfo info : infos) {
            urls.add(info.getUrl());
        }
        return urls;
    }

    /**
     * 抓取图片信息
     *
     * @param searchText 搜索词
     * @param count      抓取数量
     * @return 图片信息列表
     */
    public List<ImageInfo> fetchImages(String searchText, Integer count) {
        // 参数校验
        if (StrUtil.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        }
        if (count == null || count <= 0) {
            count = DEFAULT_COUNT;
        }
        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        List<ImageInfo> result = new ArrayList<>();

        try {
            // 构建请求 URL - 使用 cn.bing.com
            String fetchUrl = String.format("%s?q=%s&mmasync=1", BING_CN_IMAGE_URL, searchText);
            log.info("开始抓取 Bing 图片，URL: {}", fetchUrl);

            // 使用 Jsoup 直接获取页面
            Document document = Jsoup.connect(fetchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();

            // 获取图片容器 div.dgControl
            Element div = document.getElementsByClass("dgControl").first();
            if (ObjUtil.isNull(div)) {
                log.error("未找到 dgControl 元素");
                return result;
            }

            // 新版 Bing 结构：div.dgControl > ul.dgControl_list > li > div.iuscp > div.imgpt > a.iusc
            // 图片 URL 在 a 标签的 m 属性中（JSON 格式）
            Elements linkElements = div.select("a.iusc");
            log.info("从 Bing 抓取到 {} 个图片链接元素", linkElements.size());

            for (Element linkElement : linkElements) {
                // 获取 m 属性（JSON 格式）
                String mAttr = linkElement.attr("m");
                if (StrUtil.isBlank(mAttr)) {
                    continue;
                }

                try {
                    // 解析 JSON 获取 murl
                    JSONObject jsonObject = JSONUtil.parseObj(mAttr);
                    String murl = jsonObject.getStr("murl");
                    if (StrUtil.isBlank(murl)) {
                        continue;
                    }

                    // URL 解码
                    murl = URLDecoder.decode(murl, StandardCharsets.UTF_8);

                    // 处理图片 URL，去掉 ? 后面的参数
                    int questionMarkIndex = murl.indexOf("?");
                    if (questionMarkIndex > -1) {
                        murl = murl.substring(0, questionMarkIndex);
                    }

                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.setUrl(murl);
                    imageInfo.setTitle(jsonObject.getStr("t"));
                    imageInfo.setDescription(jsonObject.getStr("desc"));

                    // 推断文件扩展名
                    String ext = getFileExtension(murl);
                    imageInfo.setExtension(ext);

                    result.add(imageInfo);

                    if (result.size() >= count) {
                        break;
                    }

                } catch (Exception e) {
                    log.warn("解析图片 JSON 失败: {}", e.getMessage());
                }
            }

            log.info("成功解析 {} 个有效图片信息", result.size());

        } catch (IOException e) {
            log.error("抓取 Bing 图片异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取图片失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据图片 URL 推断文件扩展名
     * 如果 URL 中没有扩展名，尝试通过 HTTP 头获取 Content-Type
     */
    private String getFileExtension(String imageUrl) {
        // 先从 URL 中提取扩展名
        String lowerUrl = imageUrl.toLowerCase();
        if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) {
            return ".jpg";
        } else if (lowerUrl.contains(".png")) {
            return ".png";
        } else if (lowerUrl.contains(".webp")) {
            return ".webp";
        } else if (lowerUrl.contains(".gif")) {
            return ".gif";
        } else if (lowerUrl.contains(".bmp")) {
            return ".bmp";
        }

        // URL 中没有扩展名，尝试通过 HTTP 头获取
        try {
            String contentType = HttpUtil.createRequest(Method.HEAD, imageUrl).execute().header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                    return ".jpg";
                } else if (contentType.contains("png")) {
                    return ".png";
                } else if (contentType.contains("webp")) {
                    return ".webp";
                } else if (contentType.contains("gif")) {
                    return ".gif";
                } else if (contentType.contains("bmp")) {
                    return ".bmp";
                }
            }
        } catch (Exception e) {
            log.debug("无法获取图片 Content-Type: {}", e.getMessage());
        }

        // 默认使用 .jpg
        return ".jpg";
    }

    /**
     * 图片信息内部类
     */
    @Data
    public static class ImageInfo {
        /**
         * 图片 URL
         */
        private String url;

        /**
         * 图片标题
         */
        private String title;

        /**
         * 图片描述
         */
        private String description;

        /**
         * 文件扩展名（带点，如 .jpg）
         */
        private String extension = ".jpg";
    }
}