package com.xuecheng.content.feignclient;


import com.xuecheng.search.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ClassName: SearchServiceClient
 * Package: com.xuecheng.content.feignclient
 * Description:
 *
 * @Author wick
 * @Create 2025/2/9 18:15
 * @Version 1.0
 */
@FeignClient(value = "search")
@RequestMapping("/search")
public interface SearchServiceClient {
    @PostMapping("/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
