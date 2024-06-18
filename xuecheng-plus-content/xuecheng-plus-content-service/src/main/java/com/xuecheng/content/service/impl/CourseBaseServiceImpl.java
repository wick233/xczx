package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseMarketService;
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
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CourseMarketService courseMarketService;

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
    @Transactional
    @Override
    public CourseBaseInfoDto addCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //参数校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }
        //新增对象
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setStatus("203001");
        courseBase.setAuditStatus(("202002"));
        //设置公司id
        courseBase.setCompanyId(companyId);
        //添加时间
        courseBase.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBase);
        Long courseId = courseBase.getId();
        //课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        courseMarket.setId(courseId);
        //收费的话价格必须>0
        Float price = addCourseDto.getPrice();
        if(addCourseDto.getCharge().equals("201001")){
            if(price.floatValue()<=0||price==null){
                throw new XueChengPlusException("课程设置了收费价格不能为空且必须大于0");
            }
        }
        int insert1 = courseMarketMapper.insert(courseMarket);
        if(insert<0||insert1<0){
            throw new XueChengPlusException("新增课程基本信息失败");
        }
        //添加成功
        //返回添加的课程信息
        return getCourseBaseInfo(courseId);
    }

    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseBase==null){
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //查询课程分类名称
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfoById(Long courseId) {
        CourseBaseInfoDto dto = new CourseBaseInfoDto();
        LambdaQueryWrapper<CourseMarket> queryWrapper = new LambdaQueryWrapper<>();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        queryWrapper.eq(CourseMarket::getId,courseId);
        CourseMarket courseMarket = courseMarketMapper.selectOne(queryWrapper);
        if(courseBase==null||courseMarket==null){
            return null;
        }
        BeanUtils.copyProperties(courseBase, dto);
        BeanUtils.copyProperties(courseMarket, dto);
        dto.setMtName(courseCategoryMapper.selectById(courseBase.getMt()).getName());
        dto.setStName(courseCategoryMapper.selectById(courseBase.getSt()).getName());
        return dto;
    }
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(long companyId, EditCourseDto editCourseDto) {
        Long id = editCourseDto.getId();
        CourseBase courseBaseUpdate = courseBaseMapper.selectById(id);
        if(companyId != courseBaseUpdate.getCompanyId()){
            throw new XueChengPlusException("只允许修改本机构的课程");
        }
        BeanUtils.copyProperties(editCourseDto,courseBaseUpdate);
        //更新
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBaseUpdate);
        //更新课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if(courseMarket==null){
            courseMarket = new CourseMarket();
        }
        courseMarket.setId(id);
        courseMarket.setCharge(editCourseDto.getCharge());
        //收费课程必须写价格
        String charge = courseMarket.getCharge();
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price==null||price.floatValue()<=0){
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        //保存课程营销信息,没有则添加,有责更新
        boolean saveOrUpdate = courseMarketService.saveOrUpdate(courseMarket);
        return getCourseBaseInfo(id);
    }

}
