package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-06-05
 */
public interface CoursePublishService extends IService<CoursePublish> {
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
