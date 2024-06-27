package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;
    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        //生成文件id，文件的md5值
        String fileId = DigestUtils.md5Hex(bytes);
        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        //构造objectname
        if (StringUtils.isEmpty(objectName)) {
            objectName = fileId + filename.substring(filename.lastIndexOf('.'));
        }
        if (StringUtils.isEmpty(folder)) {
            //通过日期构造文件存储路径
//            folder = getFileFolder();
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf('/') < 0) {
            //如果没有以/结尾，则自动添加
            folder = folder + "/";
        }
        //对象名称
        objectName = folder + objectName;
        MediaFiles mediaFiles = null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucket_Files).object(objectName)
                    //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(uploadFileParamsDto.getContentType())
                    .build();
            minioClient.putObject(putObjectArgs);
            //从数据库中查询文件
            MediaFiles files = mediaFilesMapper.selectById(fileId);
            if (files == null) {
                files = new MediaFiles();
                //拷贝基本信息
                BeanUtils.copyProperties(uploadFileParamsDto, files);
                files.setFileId(fileId);
                files.setId(fileId);
                files.setCompanyId(companyId);
                files.setUrl("/" + bucket_Files + "/" + objectName);
                files.setBucket(bucket_Files);
                files.setCreateDate(LocalDateTime.now());
                files.setStatus("1");
                int insert = mediaFilesMapper.insert(files);
                if (insert < 0) {
                    XueChengPlusException.cast("保存文件信息失败");
                }
                UploadFileResultDto resultDto = new UploadFileResultDto();
                BeanUtils.copyProperties(files, resultDto);
                return resultDto;
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传过程中出错");
        }

        return null;
    }

        private String getFileFolder() {
        Date date = new Date();
        //根据日期拼接目录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        //获取当前日期字符串
        String dateStr = sdf.format(new Date());
        return dateStr+'/';
    }
    //根据日期拼接目录
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(new Date());
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }

}
