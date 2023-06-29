package com.weiit.resource.common.utils;

import java.io.*;
import java.util.Date;
import java.util.UUID;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.weiit.resource.common.config.WeiitFileConfig;

import org.omg.CORBA.SystemException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Weiit团队图片上传存储类,目前支持腾讯云-万象优图(Qcloud)、阿里云(aliyun)、七牛云(qiniu)三家云存储服务商，再加本地存储支持
 *
 * @author：半个鼠标(137075251@qq.com)
 * @date：2018年3月22日
 * @version 1.0
 * @company http://www.wei-it.com 微邦互联
 */
public class WeiitFileUtil {

	static COSClient cosClient = createCli();;

	static COSClient createCli() {
		return createCli(WeiitFileConfig.getCosRegion());
	}

	static COSClient createCli(String region) {
		// 初始化用户身份信息(secretId, secretKey)
		COSCredentials cred = new BasicCOSCredentials(WeiitFileConfig.getCosSecretId(),WeiitFileConfig.getCosSecretKey());
		// 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
		ClientConfig clientConfig = new ClientConfig(new Region(region));
		// 生成cos客户端
		return new COSClient(cred, clientConfig);
	}

	static void putObjectDemo() {
		String bucketName = "wstore-1255653546";
		String key = "coupon2.txt";

		String localPath = "D:\\coupon2.txt";

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setHeader("expires", new Date(1680000000000L));

		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, new File(localPath));
		putObjectRequest.withMetadata(objectMetadata);

		com.qcloud.cos.model.PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

		System.out.println(putObjectResult.getRequestId());

		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
		COSObject cosObject = cosClient.getObject(getObjectRequest);

		cosClient.shutdown();




	}


	/**
	 * 图片存储方式一：腾讯万象优图存储服务
	 *
	 * @param data
	 * @param fileFormat
	 *            文件后缀
	 * @return
	 */
	public static String uploadFileByQcloud(byte[] data, String fileFormat) {
		// 保存目录
		String cospre = WeiitFileConfig.getCosPre();
		// 图片文件夹
		String floder = DateUtil.getCurrentDate("yyyy-MM-dd");
		// 图片名称
		String fileName = UUID.randomUUID().toString() + "." + fileFormat;
		// 图片最终路径
		String cosFilePath = "/" + cospre + "/" + floder + "/" + fileName;

		PutObjectRequest putObjectRequest = new PutObjectRequest(WeiitFileConfig.getCosBucketName(), cosFilePath, byteTransIns(data),null);
		//设置存储类型 默认标准型
		putObjectRequest.setStorageClass(StorageClass.Standard);
		//获得到客户端
		try {
			com.qcloud.cos.model.PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			//putObjectResult 会返回etag
			String eTag = putObjectResult.getETag();
		} catch (CosServiceException e) {
			throw new CosServiceException(e.getMessage());
		} catch (CosClientException e) {
			throw new CosClientException(e.getMessage());
		}
		cosClient.shutdown();

		return cosFilePath;
	}

	public String uploadFileByQcloud(byte[] data, String fileFormat,
			String cosFilePath) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(WeiitFileConfig.getCosBucketName(), cosFilePath, byteTransIns(data),null);
		//设置存储类型 默认标准型
		putObjectRequest.setStorageClass(StorageClass.Standard);
		//获得到客户端
		try {
			com.qcloud.cos.model.PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			//putObjectResult 会返回etag
			String eTag = putObjectResult.getETag();
		} catch (CosServiceException e) {
			throw new CosServiceException(e.getMessage());
		} catch (CosClientException e) {
			throw new CosClientException(e.getMessage());
		}
		cosClient.shutdown();
		return cosFilePath;

	}


	public static InputStream byteTransIns(byte[] buf){
		InputStream sbs = new ByteArrayInputStream( buf);
		return sbs;
	}

	/**
	 * 图片存储方式一：腾讯万象优图存储服务
	 *
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	public String uploadFileByQcloud(MultipartFile multipartFile) throws IOException {
		//判断文件不为空
		if (ObjectUtils.isEmpty(multipartFile) || multipartFile.getSize()<=0){
			throw new IOException("未指定文件!");
		}
		File localFile = null;
		String originalFilename = null;
		String[] filename = null;
		try {
			originalFilename = multipartFile.getOriginalFilename();
			filename  = originalFilename.split("\\.");
			localFile = File.createTempFile(filename[0], filename[1]);
			//将localFile这个文件所指向的文件  上传到对应的目录
			multipartFile.transferTo(localFile);
			localFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			//文件上传失败就返回错误响应
			throw new IOException("上传失败!");
		}
		if(localFile == null){
			throw new IOException("临时文件为空！");
		}

		String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
				// 保存目录
		String cospre = WeiitFileConfig.getCosPre();
		// 图片文件夹
		String floder = DateUtil.getCurrentDate("yyyy-MM-dd");
		// 图片名称
		String fileName = UUID.randomUUID().toString() + "." + suffix;
		// 图片最终路径 // PutObjectRequest(参数1,参数2,参数3)参数1:存储桶,参数2:指定腾讯云的上传文件路径,参数3:要上传的文件
		String cosFilePath = "/" + cospre + "/" + floder + "/" + fileName;

		//2.【将文件上传到腾讯云】

		PutObjectRequest putObjectRequest = new PutObjectRequest(WeiitFileConfig.getCosBucketName(), cosFilePath, localFile);
		//设置存储类型 默认标准型
		putObjectRequest.setStorageClass(StorageClass.Standard);
		//获得到客户端
		try {
			com.qcloud.cos.model.PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
			//putObjectResult 会返回etag
			String eTag = putObjectResult.getETag();
		} catch (CosServiceException e) {
			throw new CosServiceException(e.getMessage());
		} catch (CosClientException e) {
			throw new CosClientException(e.getMessage());
		}
		cosClient.shutdown();

		String url = WeiitFileConfig.getCosDomain() + "/" + cosFilePath;
		System.out.println("上传地址"+url);
		return cosFilePath;


//		byte[] data = file.getBytes();
//		String oldFileName = file.getOriginalFilename();
//		String suffix = oldFileName.substring(oldFileName.lastIndexOf(".") + 1);
//		// 初始化客户端配置
//		ClientConfig clientConfig = new ClientConfig();
//		// 设置bucket所在的区域，比如广州(gz), 天津(tj)
//		clientConfig.setRegion(WeiitFileConfig.getCosRegion());
//		// 初始化秘钥信息
//		Credentials cred = new Credentials(Long.parseLong(WeiitFileConfig
//				.getCosAppId()), WeiitFileConfig.getCosSecretId(),
//				WeiitFileConfig.getCosSecretKey());
//		// 初始化cosClient
//		COSClient cosClient = new COSClient(clientConfig, cred);
//
//		// 保存目录
//		String cospre = WeiitFileConfig.getCosPre();
//		// 图片文件夹
//		String floder = DateUtil.getCurrentDate("yyyy-MM-dd");
//		// 图片名称
//		String fileName = UUID.randomUUID().toString() + "." + suffix;
//		// 图片最终路径
//		String cosFilePath = "/" + cospre + "/" + floder + "/" + fileName;
//
//		UploadFileRequest overWriteFileRequest = new UploadFileRequest(
//				WeiitFileConfig.getCosBucketName(), cosFilePath, data);
//		overWriteFileRequest.setInsertOnly(InsertOnly.OVER_WRITE);
//		String overWriteFileRet = cosClient.uploadFile(overWriteFileRequest);
//		cosClient.shutdown();
//		System.out.println("overwrite file ret:" + overWriteFileRet);
//		return cosFilePath;

	}

	public String uploadFileByQcloud(MultipartFile file, String cosFilePath)
			throws IOException {

		return "";
//		byte[] data = file.getBytes();
//		String oldFileName = file.getOriginalFilename();
//		String suffix = oldFileName.substring(oldFileName.lastIndexOf(".") + 1);
//		// 初始化客户端配置
//		ClientConfig clientConfig = new ClientConfig();
//		// 设置bucket所在的区域，比如广州(gz), 天津(tj)
//		clientConfig.setRegion(WeiitFileConfig.getCosRegion());
//		// 初始化秘钥信息
//		Credentials cred = new Credentials(Long.parseLong(WeiitFileConfig
//				.getCosAppId()), WeiitFileConfig.getCosSecretId(),
//				WeiitFileConfig.getCosSecretKey());
//		// 初始化cosClient
//		COSClient cosClient = new COSClient(clientConfig, cred);
//
//		UploadFileRequest overWriteFileRequest = new UploadFileRequest(
//				WeiitFileConfig.getCosBucketName(), cosFilePath, data);
//		overWriteFileRequest.setInsertOnly(InsertOnly.OVER_WRITE);
//		String overWriteFileRet = cosClient.uploadFile(overWriteFileRequest);
//		cosClient.shutdown();
//		System.out.println("overwrite file ret:" + overWriteFileRet);
//		return cosFilePath;

	}

	/**
	 * 图片存储方式二：阿里云图片存储服务
	 *
	 * @param file
	 *            文件后缀
	 * @return
	 */
	public String uploadFileByAliyun(MultipartFile file) throws IOException {

		byte[] data = file.getBytes();
		String oldFileName = file.getOriginalFilename();
		String suffix = oldFileName.substring(oldFileName.lastIndexOf(".") + 1);

		// 保存目录
		String cospre = WeiitFileConfig.getOssPre();
		// 图片文件夹
		String floder = DateUtil.getCurrentDate("yyyy-MM-dd");
		// 图片名称
		String fileName = UUID.randomUUID().toString() + "." + suffix;
		// 图片最终路径
		String cosFilePath = cospre + "/" + floder + "/" + fileName;

		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = WeiitFileConfig.getOssEndPoint();
		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录
		// https://ram.console.aliyun.com 创建。
		String accessKeyId = WeiitFileConfig.getOssAccessKeyId();
		String accessKeySecret = WeiitFileConfig.getOssAccessKeySecret();

		// 创建OSSClient实例。
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId,
				accessKeySecret);

		ossClient.putObject(WeiitFileConfig.getOssBucketName(), cosFilePath,
				new ByteArrayInputStream(data));

		// 关闭OSSClient。
		ossClient.shutdown();
		return cosFilePath;

	}

	public String uploadFileByAliyun(MultipartFile file, String cosFilePath)
			throws IOException {

		byte[] data = file.getBytes();
		String oldFileName = file.getOriginalFilename();
		String suffix = oldFileName.substring(oldFileName.lastIndexOf(".") + 1);

		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = WeiitFileConfig.getOssEndPoint();
		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录
		// https://ram.console.aliyun.com 创建。
		String accessKeyId = WeiitFileConfig.getOssAccessKeyId();
		String accessKeySecret = WeiitFileConfig.getOssAccessKeySecret();

		// 创建OSSClient实例。
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId,
				accessKeySecret);

		ossClient.putObject(WeiitFileConfig.getOssBucketName(), cosFilePath,
				new ByteArrayInputStream(data));

		// 关闭OSSClient。
		ossClient.shutdown();
		return cosFilePath;

	}

	/**
	 * 图片存储方式二：阿里云图片存储服务
	 *
	 * @param data
	 * @param fileFormat
	 *            文件后缀
	 * @return
	 */
	public static String uploadFileByAliyun(byte[] data, String fileFormat) {

		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = WeiitFileConfig.getOssEndPoint();
		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录
		// https://ram.console.aliyun.com 创建。
		String accessKeyId = WeiitFileConfig.getOssAccessKeyId();
		String accessKeySecret = WeiitFileConfig.getOssAccessKeySecret();

		// 创建OSSClient实例。
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId,
				accessKeySecret);

		// 保存目录
		String cospre = WeiitFileConfig.getOssPre();
		// 图片文件夹
		String floder = DateUtil.getCurrentDate("yyyy-MM-dd");
		// 图片名称
		String fileName = UUID.randomUUID().toString() + "." + fileFormat;
		// 图片最终路径
		String cosFilePath = cospre + "/" + floder + "/" + fileName;
		ossClient.putObject(WeiitFileConfig.getOssBucketName(), cosFilePath,
				new ByteArrayInputStream(data));

		// 关闭OSSClient。
		ossClient.shutdown();
		return cosFilePath;

	}

	public String uploadFileByAliyun(byte[] data, String fileFormat,
			String cosFilePath) {

		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = WeiitFileConfig.getOssEndPoint();
		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录
		// https://ram.console.aliyun.com 创建。
		String accessKeyId = WeiitFileConfig.getOssAccessKeyId();
		String accessKeySecret = WeiitFileConfig.getOssAccessKeySecret();

		// 创建OSSClient实例。
		OSSClient ossClient = new OSSClient(endpoint, accessKeyId,
				accessKeySecret);

		ossClient.putObject(WeiitFileConfig.getOssBucketName(), cosFilePath,
				new ByteArrayInputStream(data));

		// 关闭OSSClient。
		ossClient.shutdown();
		return cosFilePath;

	}

	/**
	 * 图片存储方式四：本地存储服务
	 *
	 * @param data
	 * @param fileFormat
	 *            文件后缀
	 * @return
	 */
	public String uploadFileByLocal(byte[] data, String fileFormat) {

		return null;

	}

	public static void main(String[] args) {
		putObjectDemo();
	}


//	public static void main(String[] args) throws FileNotFoundException {
//		// Endpoint以杭州为例，其它Region请按实际情况填写。
//		String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";
//		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录
//		// https://ram.console.aliyun.com 创建。
//		String accessKeyId = "LTAIx3ruNK9IsRFB";
//		String accessKeySecret = "vyLKu0pGLwVhd64PXsWWUxzSowxrEb";
//
//		// 创建OSSClient实例。
//		OSSClient ossClient = new OSSClient(endpoint, accessKeyId,
//				accessKeySecret);
//
//		// 上传文件流。
//		InputStream inputStream = new FileInputStream("D:\\33.png");
//		PutObjectResult result = ossClient.putObject("bluetax", "blue.png",
//				inputStream);
//		// System.out.println(result.getResponse().getUri());
//		// 关闭OSSClient。
//		ossClient.shutdown();
//	}

}
