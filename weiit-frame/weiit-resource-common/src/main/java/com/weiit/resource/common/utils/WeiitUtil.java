package com.weiit.resource.common.utils;


import java.io.IOException;




import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.aliyuncs.http.HttpResponse;
import com.weiit.resource.common.config.WeiitFileConfig;
import com.weiit.resource.common.config.WeiitLogisticsConfig;
import com.weiit.resource.common.config.WeiitSMSConfig;

import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;
/**
 * 微邦互联工具类
 * 
 * @author 半个鼠标
 * @date：2017年2月14日 上午2:12:48
 * @version 1.0
 * @company http://www.wei-it.com
 */
public class WeiitUtil {

	/**
	 * 短信公共方法，具体要依据WeiitSMSConfig的open值决定调用哪个服务商的短信支持
	 * 
	 * @param mobileNums 表示手机号码
	 *            ，可以是多个，多个用逗号分开，比如“15622720546,18565660736”
	 * @param templateId 表示短信模板id
	 *            ，模板中存在{1}，{2}这样的占位符
	 * @param contents
	 *            表示需要替换的内容，是一个字符串数组，每个字符串数组按照顺利会替换模板中的占位符{1}，{2}
	 * @return 发送成功或者发送失败
	 */
	public static boolean sendMobileMessage(String mobileNums,
			String templateId, String[] contents) {
		
		switch (WeiitSMSConfig.getSmsOpen()) {
		case 1:
			return WeiitSMSUtil.sendMobileMessageByQcloud(mobileNums,
					templateId, contents);
		case 2:
			return WeiitSMSUtil.sendMobileMessageByAliyun(mobileNums,
					templateId, contents[0]);
		default:
			return false;
		}
		
	}
	
	
	/**
	 * 图片存储公共方法，具体要依据WeiitImageConfig的open值决定图片存储方式
	 * @param data 表示图片的字节流
	 * @return 图片上传后端图片地址
	 */
	public static String uploadFile(byte[] data, String fileFormat) throws IOException  {
		switch (Integer.parseInt(WeiitFileConfig.getFileUploadOpen())) {
		case 1:
			return WeiitFileUtil.uploadFileByQcloud(data,fileFormat);
		case 2:
			return WeiitFileUtil.uploadFileByAliyun(data,fileFormat);
		default:
			return null;
		}
		
	}
	public static String uploadFile(byte[] data, String fileFormat,String cosFilePath) throws IOException  {
			
			WeiitFileUtil util=new WeiitFileUtil();
			
			switch (Integer.parseInt(WeiitFileConfig.getFileUploadOpen())) {
			case 1:
				return util.uploadFileByQcloud(data,fileFormat,cosFilePath);
			case 2:
				return util.uploadFileByAliyun(data,fileFormat,cosFilePath);
			default:
				return null;
			}
			
		}
	
	/**
	 * 图片存储公共方法，具体要依据WeiitImageConfig的open值决定图片存储方式
	 * @param file MultipartFile
	 * @return 图片上传后端图片地址
	 * @throws IOException 
	 */
	public static String uploadFile(MultipartFile file) throws IOException {
		
		WeiitFileUtil util=new WeiitFileUtil();
		
		switch (Integer.parseInt(WeiitFileConfig.getFileUploadOpen())) {
		case 1:
			return util.uploadFileByQcloud(file);
		case 2:
			return util.uploadFileByAliyun(file);
		default:
			return null;
		}
		
	}
	
	public static String uploadFile(MultipartFile file,String cosFilePath) throws IOException {
			
			WeiitFileUtil util=new WeiitFileUtil();
			
			switch (Integer.parseInt(WeiitFileConfig.getFileUploadOpen())) {
			case 1:
				return util.uploadFileByQcloud(file,cosFilePath);
			case 2:
				return util.uploadFileByAliyun(file,cosFilePath);
			default:
				return null;
			}
			
		}
	
	public static String getFileDomain() {
		
		switch (Integer.parseInt(WeiitFileConfig.getFileUploadOpen())) {
		case 1:
			return WeiitFileConfig.getCosDomain();
		case 2:
			return WeiitFileConfig.getOssDomain();
		default:
			return null;
		}
		
	}
	
	/**
	 * 物流公共查询方法，具体要依据WeiitLogisticsConfig的open值决定走哪家通道查询
	 * @param expCode 物流公司编号
	 * @param expNo 物流单号
	 * @return 物流轨迹信息
	 */
	public static String getLogistics(String expCode, String expNo) throws Exception{
		
		switch (Integer.parseInt(WeiitLogisticsConfig.getExpressOpen())) {
		case 1:
			return WeiitLogisticsUtil.getOrderTracesByJson(expCode, expNo);
		case 2:
			return WeiitLogisticsUtil.getExpressInfoByAli(expCode, expNo);
		default:
			return null;
		}
		
	}
	
	/**
	 * 读取配置文件方法
	 * @param key
	 * @return
	 */
	public static String getPropertiesKey(String key){
		Properties prop = new Properties();   
        InputStream in = WeiitUtil.class.getClassLoader().getResourceAsStream("resource.properties");   
        try {   
        	if(in!=null){
        		 prop.load(in);
        		 if ( prop.getProperty(key)==null){
        		 	return null;
				 }
                 return prop.getProperty(key).trim(); 
        	}
        	return null;
             
        } catch (IOException e) {   
            e.printStackTrace();   
            return null;
        }  
		
	}
	
	/**
	 * 读取配置文件
	 * @param filePath
	 * @param key
	 * @return
	 */
	public static String getPropertiesKey(String filePath,String key){
		Properties prop = new Properties();   
        InputStream in = Object.class.getResourceAsStream(filePath);   
        try {   
            prop.load(in);   
            return prop.getProperty(key).trim();   
        } catch (IOException e) {   
            e.printStackTrace();   
            return null;
        }  
		
	}
	
	public static void main(String[] args) {
		String [] msg ={"{\"code\":\"123\"}"};
		boolean isSend = WeiitUtil.sendMobileMessage("18520852017", "SMS_168345359", msg);
		System.out.println("发送状态"+isSend);
	}
}
