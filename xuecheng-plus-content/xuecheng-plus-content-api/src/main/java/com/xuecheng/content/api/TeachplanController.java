package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程计划编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
 @Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
 @RestController
public class TeachplanController {
     @Autowired
     private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        List<TeachplanDto> teachplanDtos = teachplanService.getTreeNodes(courseId);
        return teachplanDtos;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId){
        teachplanService.deleteTeachPlan(teachPlanId);
    }


    @ApiOperation(value = "课程计划与媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }


    //TODO:解除课程计划和媒资信息绑定    小节上移下移
    @ApiOperation(value = "解除课程计划与媒资信息绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void delAssociationMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId) {
        teachplanService.delAssociationMedia(teachPlanId, mediaId);
    }

    @ApiOperation("上移课程计划")
    @PostMapping("/teachplan/moveup/{teachPlanId}")
    public void moveUpTeachPlan(@PathVariable Long teachPlanId){
        teachplanService.moveUpTeachPlan(teachPlanId);
    }


    @ApiOperation("下移课程计划")
    @PostMapping("/teachplan/movedown/{teachPlanId}")
    public void moveDownTeachPlan(@PathVariable Long teachPlanId){
        teachplanService.moveDownTeachPlan(teachPlanId);
    }
}
