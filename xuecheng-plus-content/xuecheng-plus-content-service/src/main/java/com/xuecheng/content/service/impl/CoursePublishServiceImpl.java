package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    CourseBaseService   courseBaseService;
    @Autowired
    TeachplanService teachplanService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper  coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfoById(courseId);
        List<TeachplanDto> teachplanDtos = teachplanService.getTreeNodes(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId,Long courseId) {
        // 校验课程
        // 查询课程基本信息|课程营销信息|课程计划信息
        CourseBaseInfoDto courseBaseInfoById = courseBaseService.getCourseBaseInfoById(courseId);
        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBaseInfoById == null){
            XueChengPlusException.cast("课程不存在");
        }
        // 校验课程状态
        if("202003".equals(courseBaseInfoById.getAuditStatus())){
            XueChengPlusException.cast("课程审核中请等待");
        }
        //本机构只允许提交本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        // 课程图片,课程计划为空不允许提交
        if(courseBaseInfoById.getPic() == null){
            XueChengPlusException.cast("请上传课程图片");
        }
        if(treeNodes == null || treeNodes.size() == 0){
            XueChengPlusException.cast("课程计划为空");
        }
        // 查询预发布表课程信息,有则更新无则新增
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoById,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        String treeNodesJson = JSON.toJSONString(treeNodes);
        coursePublishPre.setTeachplan(treeNodesJson);
        coursePublishPre.setMarket(courseMarketJson);
        coursePublishPre.setStatus("202003");
        CoursePublishPre publishPre = coursePublishPreMapper.selectById(courseId);
        if (publishPre == null){
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        // 更新课程基本信息表 课程状态未已提交
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
    @Transactional
    @Override
    public void publish(long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        if (!"202004".equals(courseBase.getAuditStatus())){
            XueChengPlusException.cast("课程审核未通过");
        }
        if (courseBase.getCompanyId() != companyId){
            XueChengPlusException.cast("不允许发布其它机构的课程");
        }
        // 保存课程发布信息到发布表
        saveCoursePublish(courseId);
        // TODO:保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage course_publish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (course_publish == null){
            XueChengPlusException.cast("添加消息失败");
        }
    }

    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据不存在");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
        CoursePublish publish = coursePublishMapper.selectById(courseId);
        if (publish == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
    }
}
