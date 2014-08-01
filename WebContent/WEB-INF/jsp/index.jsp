<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>index 페이지</title>
	
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
	<script type="text/javascript">
	(function(window, $, undefined){
		$(document).ready(function(){
			
			var sendForm = window.document.sendForm;
			
			$(sendForm).submit(function(){
				$.ajax({
					url: "/mypeople/callback.htm",
					type: "POST",
					data: $(sendForm).serialize(),
					complete: function(){
						alert("전송완료");
					}
				});
				
				event.preventDefault();
			});
		});
	})(window, jQuery);
	</script>
</head>
<body>
<h2>마이피플 봇 API 예제</h2>
등록된 회원
<ul>
<c:forEach var="b" items="${buddys }">
	<li>${b.key } : ${b.value }</li>
</c:forEach>
</ul>
<div>
<h3>1:1 대화 메시지 보내기</h3>
<form method='post' id="sendForm" name="sendForm">
	<input type='hidden' name='action' value='sendFromMessage'></input>
	buddyId : <input type='text' name='buddyId' value='' /><br />
	content : <input type='text' name='content' value='hello' /><br /> 
	<input type="submit" value='메시지 전송 실행'/>
</form>
</div>
<hr />
<div>	
<h3>1대1 대화 파일 보내기</h3>
jpg, gif, png만 가능(그룹대화에 전송할 경우 buudyId대신 groupId사용)
<form method="post" name="sendFileForm" enctype="multipart/form-data" action='/mypeople/sendFile.htm'>
<input type='hidden' name='action' value='sendFile' />
buddyId : <input type='text' name='buddyId' value='' /><br />
file : <input type='file' name='upload' />
<input type='hidden' name='content' value='attach' /><br /> 
<input type='submit' value='파일 전송 실행'/>
</form>	
</div>
<hr />
<div>	
<h3>파일 다운로드</h3>
파일 저장경로는 "서버경로/download" 폴더 입니다
<form method='post' action='/mypeople/download.htm'>
<input type='hidden' name='action' value='download'></input>
fileId : <input type='text' name='fileId' value='myp_moi:53DB2E2C0352D7001D' />
<input type='submit' value='파일 다운로드 실행'/>
</form>	
</div>

</body>
</html>