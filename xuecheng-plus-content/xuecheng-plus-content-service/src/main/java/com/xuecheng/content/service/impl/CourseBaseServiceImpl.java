package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //课程名称模糊查询
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()), CourseBase::getName, queryCourseParams.getCourseName());
        //课程审核状态
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParams.getAuditStatus());
        //课程发布状态
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getPublishStatus()), CourseBase::getStatus, queryCourseParams.getPublishStatus());
        //构造分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //执行查询
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, lambdaQueryWrapper);
        //获取数据列表
        List<CourseBase> pageRecords = courseBasePage.getRecords();
        //获取总条数
        long total = courseBasePage.getTotal();
        //封装结果集
        PageResult<CourseBase> PageResult = new PageResult<>(pageRecords,total,pageParams.getPageNo(),pageParams.getPageSize());
        return PageResult;
    }
}
