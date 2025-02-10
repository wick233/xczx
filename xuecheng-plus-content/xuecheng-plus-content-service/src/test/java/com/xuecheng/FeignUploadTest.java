package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * ClassName: FeignUploadTest
 * Package: com.xuecheng
 * Description:
 *
 * @Author wick
 * @Create 2024/8/14 21:09
 * @Version 1.0
 */
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Test
    public void testUpload(){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\120.html"));
        mediaServiceClient.uploadFile(multipartFile,"course","120.html");
    }
}
