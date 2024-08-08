package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: CoursePreviewDto
 * Package: com.xuecheng.content.model.dto
 * Description:课程预览dto
 *
 * @Author wick
 * @Create 2024/8/8 21:17
 * @Version 1.0
 */
@Data
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    private CourseBaseInfoDto courseBase;
    //课程计划信息
    private List<TeachplanDto> teachplans;

    //课程师资信息..

}
