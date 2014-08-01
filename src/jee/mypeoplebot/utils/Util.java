package jee.mypeoplebot.utils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Util {
	
	/**
	 * url로 접근 해서 source를 받아오는 메소드 (POST방식)
	 * 타 웹사이트의 API를 Ajax에서 불러오기 위한 방법으로 만들었음.
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String getHttp2String(String url, Map<String, String> param) throws Exception{
		
		List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
		if(param != null && !param.isEmpty()){
			for(String key : param.keySet()){
				parameters.add( new BasicNameValuePair(key, param.get(key)) );
			}
		}
		UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
		
		HttpPost post = new HttpPost(url);
		post.setEntity(reqEntity);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(post);
		
		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		
		return new String(out.toByteArray(), Charset.forName("UTF-8"));
	}
	
	/**
	 * url로 접근 해서 source를 받아오는 메소드 (GET방식)
	 * 타 웹사이트의 API를 Ajax에서 불러오기 위한 방법으로 만들었음.
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public static String getHttpGET2String(String url, Map<String, String> param) throws Exception{
		
		if(param != null && !param.isEmpty()){
			for(String key : param.keySet()){
				if(url.lastIndexOf("?")>-1){
					url = url+"&"+key+"="+URLEncoder.encode(param.get(key), "UTF-8");
				}else{
					url = url+"?"+key+"="+URLEncoder.encode(param.get(key), "UTF-8");
				}
			}
		}
		HttpGet get = new HttpGet(url);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(get);
		
		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		
		return new String(out.toByteArray(), Charset.forName("UTF-8"));
	}
}
