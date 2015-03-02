package jee.mypeoplebot.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jee.mypeoplebot.utils.Util;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


@Controller
public class MyPeopleController extends MultiActionController{
	/**
	 * 마플정보 message.bot/
	 */
	private static final String API_URL_PREFIX = "https://apis.daum.net";
	private static final String MYPEOPLE_BOT_APIKEY = "[MYPEOPLE BOT API]";
	private static final String API_URL_POSTFIX = "&apikey="+MYPEOPLE_BOT_APIKEY;
	
	@RequestMapping("/mypeople/callback")
	public void callback( HttpServletRequest req, HttpServletResponse res){
		String action = ServletRequestUtils.getStringParameter(req, "action", null);
		
		if("addBuddy".equals(action)){						// 친구로 추가
			greetingMessageToBuddy(req);
		} else if("sendFromMessage".equals(action)){		// 친구로 받은 메시지
			echoMessageToBuddy(req);
		} else if("createGroup".equals(action)){			// 그룹 참여
			groupCreatedMessage(req);
		} else if("inviteToGroup".equals(action)){			// 그룹에 다른 친구가 참여
			groupGreetingMeesage(req);
		} else if("exitFromGroup".equals(action)){			// 그룹에서 친구가 나감
			groupExitAlertMessage(req);
		} else if("sendFromGroup".equals(action)){			// 그룹에서 받은 메시지
			filterGroupMessage(req);
		} else {
			System.out.println("확인되지 않은 callback 호출 :: "+ action);
			List<String> names = EnumerationUtils.toList(req.getParameterNames());
			for(String name : names){
				System.out.println(name +" : "+ServletRequestUtils.getStringParameter(req, name, ""));
			}
		}
	}
	
	@RequestMapping("/mypeople/sendFile")
	public ModelAndView sendFile( HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		res.setContentType("text/plain");
		if( !(req instanceof MultipartHttpServletRequest) ){
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expected multipart request");
			return null;
		}
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
		MultipartFile imgFile = multipartRequest.getFile("upload");

		final String imgFileName = imgFile.getOriginalFilename().trim();
		File uploadDir = new File("/upload/");
		if( !uploadDir.exists() ) uploadDir.mkdir();
		
		String filePath = uploadDir.getAbsolutePath()+File.separator;
		
		long fileSize = imgFile.getSize();
		
		int pathPoint = imgFileName.lastIndexOf(".");
		String fileName = imgFileName.substring(0, pathPoint);
		String fileType = imgFileName.substring(pathPoint + 1, imgFileName.length() );
		fileType = fileType.toLowerCase();
		
		System.out.println("파일명: "+fileName+", 확장자 : "+fileType);
		System.out.println("파일사이즈 : "+fileSize);
		
		String finalFnm = filePath + imgFileName;
		if( new File(finalFnm).exists() ) finalFnm = filePath +"_"+ imgFileName;
		File file = new File(finalFnm);
		imgFile.transferTo( file );
		file.deleteOnExit();
		
		// 파일 전송
		String buddyId = multipartRequest.getParameter("buddyId");
		sendFile("buddy", buddyId, file);
	
		return new ModelAndView("redirect:/index.htm");
	}
	
	@RequestMapping("/mypeople/download")
	public ModelAndView download( HttpServletRequest req, HttpServletResponse res) throws Exception{
		String fileId = ServletRequestUtils.getStringParameter(req, "fileId", "");
		downloadFile(fileId);
		return new ModelAndView("redirect:/index.htm");
	}
	
	private void settingBuddys(HttpServletRequest req, String buddyName, String buddyId) {
		//
		ServletContext application = req.getSession().getServletContext();
		Map<String, String> buddys = (Map)application.getAttribute("buddys");
		if(buddys == null) buddys = new HashMap<String,String>();
		buddys.put(buddyName, buddyId);
		
		application.setAttribute("buddys", buddys);
	}
	
	private void greetingMessageToBuddy(HttpServletRequest req) {
		// 
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String buddyName = getBuddyName(buddyId);
		
		settingBuddys(req, buddyName, buddyId);
		
		String msg = buddyName+"님 반갑습니다. 함께하는 토즈입니다.";
		sendMessage("buddy", buddyId, msg);
	}
	
	

	private void echoMessageToBuddy(HttpServletRequest req) {
		//
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String msg = ServletRequestUtils.getStringParameter(req, "content", "");
		String buddyName = getBuddyName(buddyId);
		
		settingBuddys(req, buddyName, buddyId);
		
		System.out.println( buddyName + "("+buddyId+")님에게 메시지 받음 :: " + msg );
		
		sendMessage("buddy", buddyId, msg);
	}
	
	private void groupCreatedMessage(HttpServletRequest req) {
		//
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String s_buddys = ServletRequestUtils.getStringParameter(req, "content", "");
		String groupId = ServletRequestUtils.getStringParameter(req, "groupId", "");
		
		try{
			JSONArray buddys = new JSONArray(new JSONTokener(s_buddys));
			String buddyNames = "";
			for(int idx = 0 ; idx<buddys.length(); idx++){
				JSONObject buddy = buddys.getJSONObject(idx);
				buddyNames += (buddy.getString("name")+" ");
			}
			String msg = getBuddyName(buddyId) + "님이 새로운 그룹대화를 만들었습니다. 그룹멤버는 " + buddyNames +" 입니다. 봇을 퇴장시키려면 \"봇 퇴장\", \"bot exit\"를 입력해주세요";
			sendMessage("group", groupId, msg);
		}catch(Exception e){
			System.out.println(":: groupCreatedMessage EXCEPTION ::");
			e.printStackTrace();
		}
	}
	
	private void groupGreetingMeesage(HttpServletRequest req) {
		//
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String s_buddys = ServletRequestUtils.getStringParameter(req, "content", "");
		String groupId = ServletRequestUtils.getStringParameter(req, "groupId", "");
		
		try{
			JSONArray buddys = new JSONArray(new JSONTokener(s_buddys));
			String buddyNames = "";
			for(int idx = 0 ; idx<buddys.length(); idx++){
				JSONObject buddy = buddys.getJSONObject(idx);
				buddyNames += (buddy.getString("name")+" ");
			}
			String msg = getBuddyName(buddyId) + "님께서 " + buddyNames +"님을 초대하였습니다. 봇을 퇴장시키려면 \"봇 퇴장\", \"bot exit\"를 입력해주세요.";
			sendMessage("group", groupId, msg);
		}catch(Exception e){
			System.out.println(":: groupGreetingMeesage EXCEPTION ::");
			e.printStackTrace();
		}
	}
	
	private void groupExitAlertMessage(HttpServletRequest req) {
		//
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String groupId = ServletRequestUtils.getStringParameter(req, "groupId", "");
		
		String msg = getBuddyName(buddyId)+"님께서 퇴장 하셨습니다. 봇을 퇴장시키려면 \"봇 퇴장\", \"bot exit\"를 입력해주세요";
		sendMessage("group", groupId, msg);
		
	}
	
	private void filterGroupMessage(HttpServletRequest req) {
		//
		String buddyId = ServletRequestUtils.getStringParameter(req, "buddyId", "");
		String groupId = ServletRequestUtils.getStringParameter(req, "groupId", "");
		String msg = ServletRequestUtils.getStringParameter(req, "content", "");
		
		if(msg.indexOf("마이피플") > -1){
			sendMessage("group", groupId, getBuddyName(buddyId) + "님 마이피플 봇을 퇴장시키려면 \"봇 퇴장\", \"bot exit\"를 입력해주세요");
		}
		if( (msg.indexOf("봇") > -1 || msg.indexOf("bot") > -1)
				&& (msg.indexOf("퇴장")>-1 || msg.indexOf("exit")>-1) ){
			exitGroup(groupId);
		}
		
	}

	/**
	 * 
	 * @param target 에게 보내는 메시지면 "group", 친구에게 보내는 것인면 "buddy"
	 * @param targetId 마이피플 친구 또는 그룹아이디
	 * @param msg 전달될 메시지 내용
	 */
	private void sendMessage(String target, String targetId, String msg) {
		//
		String url = API_URL_PREFIX + "/mypeople/" + target + "/send.json";
		msg = msg.replace('\n', '\r');
		Map<String, String> params = new HashMap<String, String>();
		params.put("apikey", MYPEOPLE_BOT_APIKEY);
		params.put(target+"Id", targetId);
		params.put("content", msg);
		try{
			String json = Util.getHttp2String(url, params);
			System.out.println( "메시지전송 ::" + json );
		}catch(Exception e){
			System.out.println(":: http connected EXCEPTION ::");
			e.printStackTrace();
		}
	}

	private String getBuddyName(String buddyId) {
		//
		String url = API_URL_PREFIX + "/mypeople/profile/buddy.json";
		String name = null;
		try{
			Map<String, String> params = new HashMap<String, String>();
			params.put("buddyId", buddyId);
			params.put("apikey", MYPEOPLE_BOT_APIKEY);
			String resultJson = Util.getHttp2String(url, params);
			JSONArray buddys = new JSONObject(new JSONTokener(resultJson)).getJSONArray("buddys");
			JSONObject buddy = buddys.getJSONObject(0);
			name = buddy.getString("name"); 
			
			System.out.println(resultJson);
		}catch(Exception e){
			System.out.println(":: getBuddyName EXCEPTION ::");
			e.printStackTrace();
		}
		return name;
	}
	
	private void downloadFile(String fileId) {
		//
		String url = API_URL_PREFIX + "/mypeople/file/download.json";
		Map<String, String> params = new HashMap<String, String>();
		params.put("fileId", fileId);
		params.put("apikey", MYPEOPLE_BOT_APIKEY);
		try{
			List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
			if(params != null && !params.isEmpty()){
				for(String key : params.keySet()){
					parameters.add( new BasicNameValuePair(key, params.get(key)) );
				}
			}
			UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(parameters, "UTF-8");
			
			HttpPost post = new HttpPost(url);
			post.setEntity(reqEntity);
			
			System.out.println("executing request " + post.getRequestLine());
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(post);
			
			HttpEntity entity = response.getEntity();
			String filename=null;
			for(Header h : response.getHeaders("Content-Disposition")){
				filename = h.getValue().substring(h.getValue().lastIndexOf("filename=") + "filename=".length());
				System.out.println(h.getName()+" : "+h.getValue()+", "+filename);
			}
			
			File downloadDir = new File("/download/");
			if(!downloadDir.exists()) downloadDir.mkdir();
			FileOutputStream fout = new FileOutputStream(downloadDir.getAbsolutePath()+File.separator + filename);
			System.out.println("저장위치 : "+downloadDir.getAbsolutePath()+File.separator + filename);
			try{
				entity.writeTo(fout);
			}catch (Exception e) {
				//
				e.printStackTrace();
			}finally{
				fout.close();
			}
			
		}catch (Exception e) {
			//
			System.out.println(":: downloadFile EXCEPTION ::");
			e.printStackTrace();
		}
	}
	
	private void exitGroup(String groupId) {
		//
		String url = API_URL_PREFIX + "/mypeople/group/exit.json";
		try{
			Map<String, String> params = new HashMap<String, String>();
			params.put("groupId", groupId);
			params.put("apikey", MYPEOPLE_BOT_APIKEY);
			String resultJson = Util.getHttp2String(url, params);
			System.out.println("마이피플봇 퇴장 :: "+ resultJson);
		}catch(Exception e){
			System.out.println(":: exitGroup EXCEPTION ::");
			e.printStackTrace();
		}
	}
	

	private void sendFile(String target, String targetId, File file) {
		// 
		String url = API_URL_PREFIX + "/mypeople/" + target + "/send.json?"+target+"Id="+targetId+"&content=attach"+API_URL_POSTFIX;
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("apikey", MYPEOPLE_BOT_APIKEY);
		params.put(target+"Id", targetId);
		params.put("content", "attach");
		
		System.out.println(":: 업로드 파일 :: " + file.getAbsolutePath());
		try{
			
			HttpPost post = new HttpPost(url);
			post.setHeader("connection", "Keep-Alive");
			post.setHeader("Accept-Charset", "UTF-8");
			post.setHeader("ENCTYPE", "multipart/form-data");
			
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8") );
			multipartEntity.addPart( "attach", new FileBody(file) );
			post.setEntity(multipartEntity);
			
			System.out.println("executing request " + post.getRequestLine());
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(post);
			
			HttpEntity entity = response.getEntity();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			entity.writeTo(out);
			
			
			String json = new String(out.toByteArray(), Charset.forName("UTF-8"));
			
			System.out.println( "파일전송("+targetId+") ::" + json );
		}catch(Exception e){
			System.out.println(":: http connected EXCEPTION ::");
			e.printStackTrace();
		}
		
	}
}
