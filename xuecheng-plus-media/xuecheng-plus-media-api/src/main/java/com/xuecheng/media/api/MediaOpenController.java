package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: MediaOpenController
 * Package: com.xuecheng.media.api
 * Description:
 *
 * @Author wick
 * @Create 2024/8/8 21:49
 * @Version 1.0
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MediaFilesMapper mediaFilesManager;
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFilesManager.selectById(mediaId);
        if (mediaFiles == null||mediaFiles.getUrl() == null) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        MediaFiles playUrlByMediaId = mediaFileService.getPlayUrlByMediaId(mediaId);
        return RestResponse.success(playUrlByMediaId.getUrl());
    }
    }
