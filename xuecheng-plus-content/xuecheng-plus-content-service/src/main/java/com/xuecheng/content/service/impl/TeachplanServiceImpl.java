package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplan) {
        Long teachplanId = teachplan.getId();
        if (teachplanId != null) {
            //修改
            Teachplan selectById = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(teachplan, selectById);
            teachplanMapper.updateById(selectById);
        } else {
            //新增
            Teachplan teachPlanNew = new Teachplan();
            BeanUtils.copyProperties(teachplan, teachPlanNew);
            //取出同父同级别的课程计划数量
            Long parentid = teachplan.getParentid();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            queryWrapper.eq(Teachplan::getParentid, parentid);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachPlanNew.setOrderby(count + 1);
            teachplanMapper.insert(teachPlanNew);
        }
    }

    @Override
    public void deleteTeachPlan(Long teachPlanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        Integer grade = teachplan.getGrade();
        //大章节删除
        if (grade == 1) {
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,teachplan.getId());
            if (teachplanMapper.selectCount(queryWrapper) > 0) {
                XueChengPlusException.cast("删除失败！该章节下还存有内容！");
            }else teachplanMapper.deleteById(teachplan);
        } else if(grade == 2){
            //小章节删除
            teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachPlanId));
            teachplanMapper.deleteById(teachplan);
        }else XueChengPlusException.cast("系统数据异常");
    }

    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划ID
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade!= 2) {
            XueChengPlusException.cast("只允许二级教学计划绑定媒资文件");
        }
        Long courseId = teachplan.getCourseId();
        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
        //再添加新的媒资
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void delAssociationMedia(Long teachPlanId, String mediaId) {
        if(StringUtils.isEmpty(String.valueOf(teachPlanId))){
            XueChengPlusException.cast("教学计划id为空");
        }
        if (StringUtils.isEmpty(mediaId)){
            XueChengPlusException.cast("媒资文件id为空");
        }
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachPlanId).eq(TeachplanMedia::getMediaId, mediaId));
        if (delete == 0){
            XueChengPlusException.cast("删除失败");
        }else {
            log.debug("删除成功");
        }
    }

    @Override
    public void moveUpTeachPlan(Long teachPlanId) {
        if (StringUtils.isEmpty(String.valueOf(teachPlanId))){
            XueChengPlusException.cast("教学计划id为空");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<Teachplan>();
        wrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid).le(Teachplan::getOrderby, orderby).last("limit 1");
        Teachplan selectOne = teachplanMapper.selectOne(wrapper);
        if (selectOne == null){
            XueChengPlusException.cast("已经是最前面了");
        }
        teachplan.setOrderby(selectOne.getOrderby());
        teachplanMapper.updateById(teachplan);
        selectOne.setOrderby(orderby);
        teachplanMapper.updateById(selectOne);
    }

    @Override
    public void moveDownTeachPlan(Long teachPlanId) {
        if (StringUtils.isEmpty(String.valueOf(teachPlanId))){
            XueChengPlusException.cast("教学计划id为空");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<Teachplan>();
        wrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid).ge(Teachplan::getOrderby, orderby).last("limit 1");
        Teachplan selectOne = teachplanMapper.selectOne(wrapper);
        if (selectOne == null){
            XueChengPlusException.cast("已经是最后面了");
        }
        teachplan.setOrderby(selectOne.getOrderby());
        teachplanMapper.updateById(teachplan);
        selectOne.setOrderby(orderby);
        teachplanMapper.updateById(selectOne);
    }
}
