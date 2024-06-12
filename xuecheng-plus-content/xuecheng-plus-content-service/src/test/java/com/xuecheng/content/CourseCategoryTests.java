package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class CourseCategoryTests {
    @Resource
    CourseCategoryMapper courseCategoryMapper;
    @Resource
    CourseCategoryService courseCategoryService;

    @Test
    public void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> treeNodes = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(treeNodes);

    }
    @Test
    public void queryTreeNodes() {
        List<CourseCategoryTreeDto> treeNodes = courseCategoryService.queryTreeNodes("1");
        System.out.println(treeNodes);

    }

}
