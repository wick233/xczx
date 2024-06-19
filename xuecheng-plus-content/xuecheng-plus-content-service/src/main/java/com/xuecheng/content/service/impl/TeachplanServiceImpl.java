package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplan) {
        Long teachplanId = teachplan.getId();
        if(teachplanId!=null){
            //修改
            Teachplan selectById = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(teachplan,selectById);
            teachplanMapper.updateById(selectById);
        }else{
            //新增
            Teachplan teachPlanNew = new Teachplan();
            BeanUtils.copyProperties(teachplan,teachPlanNew);
            //取出同父同级别的课程计划数量
            Long parentid = teachplan.getParentid();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
            queryWrapper.eq(Teachplan::getParentid,parentid);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachPlanNew.setOrderby(count+1);
            teachplanMapper.insert(teachPlanNew);
        }
    }
}
