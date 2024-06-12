package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.service.CourseBaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CourseBaseTests {
    @Resource
    CourseCategoryMapper courseCategoryMapper;
    @Resource
    CourseBaseService courseBaseService;

    @Test
    public void testCourseBaseService() {
//        System.out.println(treeNodes);
    }
}
