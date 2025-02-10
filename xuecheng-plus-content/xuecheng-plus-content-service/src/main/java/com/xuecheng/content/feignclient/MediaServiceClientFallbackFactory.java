package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: MediaServiceClientFallbackFactory
 * Package: com.xuecheng.content.feignclient
 * Description:
 *
 * @Author wick
 * @Create 2024/8/29 20:07
 * @Version 1.0
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String folder, String objectName) {
                System.out.println("远程调用媒资管理服务熔断异常：" + throwable.getMessage());
                return null;
            }
        };
    }
}
