package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: CoursePublishTask
 * Package: com.xuecheng.content.service.jobhandler
 * Description:课程发布任务类
 *
 * @Author wick
 * @Create 2024/8/11 21:06
 * @Version 1.0
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //调用抽象类的方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }
    @Override
    public boolean execute(MqMessage mqMessage) {
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.parseLong(businessKey1);
        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);
        //向redis写缓存
        saveCourseCache(mqMessage,courseId);
        //向elasticsearch写索引
        saveCourseIndex(mqMessage,courseId);
        //返回true表示任务完成
        return true;
    }

    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        log.debug("开始执行elasticsearch索引,课程id:{}",courseId);
        //获取消息id
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int i = mqMessageService.getStageThree(id);
        if (i>0){
            log.debug("elasticsearch索引完成,课程id:{}",courseId);
            return;
        }
        //开始进行redis缓存
        //任务处理完成更新任务状态
        mqMessageService.completedStageThree(id);
    }

    private void saveCourseCache(MqMessage mqMessage, Long courseId) {
        log.debug("开始执行redis缓存,课程id:{}",courseId);
        //获取消息id
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int i = mqMessageService.getStageTwo(id);
        if (i>0){
            log.debug("redis缓存完成,课程id:{}",courseId);
            return;
        }
        //开始进行redis缓存
        //任务处理完成更新任务状态
        mqMessageService.completedStageTwo(id);
    }

    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始执行课程静态化,课程id:{}",courseId);
        //获取消息id
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int i = mqMessageService.getStageOne(id);
        if (i>0){
            log.debug("课程静态化完成,课程id:{}",courseId);
            return;
        }
        //开始进行课程静态化
         int error = 1/0;
        //任务处理完成更新任务状态
        mqMessageService.completedStageOne(id);
    }
}
