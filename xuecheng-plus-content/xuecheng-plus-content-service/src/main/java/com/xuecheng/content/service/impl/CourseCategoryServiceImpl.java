package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //查询数据库得到课程分类
        List<CourseCategoryTreeDto> courseCategoryTree = courseCategoryMapper.selectTreeNodes(id);
        //最终返回的列表
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //为了方便寻找子节点的父节点，建立一个map
        HashMap<String, CourseCategoryTreeDto> map = new HashMap<>();
        //将数据封装到List中,只包含根节点的一级子节点
        courseCategoryTree.stream().filter(item -> !item.getId().equals(id)).forEach(item -> {
            map.put(item.getId(), item);
            if(item.getParentid().equals(id)){
                categoryTreeDtos.add(item);
            }
            //找到该节点的父节点
            String parentid = item.getParentid();
            //找到该节点的父节点对象
            CourseCategoryTreeDto parentObj = map.get(parentid);
            if(parentObj!= null){
                //将该节点添加到父节点的子节点列表中
                List parentObjChildrenTreeNodes = parentObj.getChildrenTreeNodes();
                if(parentObjChildrenTreeNodes == null){
                    parentObj.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parentObj.getChildrenTreeNodes().add(item);
            }
        });
        System.out.println(categoryTreeDtos);
        return categoryTreeDtos;
    }
}
