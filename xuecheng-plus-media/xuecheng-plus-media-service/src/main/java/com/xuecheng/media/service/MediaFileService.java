package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * @description 上传文件
  * @param uploadFileParamsDto  上传文件信息
  * @param folder  文件目录,如果不传则默认年、月、日
  * @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件结果
  * @author Mr.M
  * @date 2022/9/12 19:31
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);


 RestResponse<Boolean> checkFile(String fileMd5);

 RestResponse<Boolean> checkChunk(String fileMd5, int chunk);

 RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);

 RestResponse mergeChunks(String fileMd5, String fileName, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

 MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket_videoFiles, String mergeFilePath);

 MediaFiles getPlayUrlByMediaId(String mediaId);
}
