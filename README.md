DaumMypeopleBot4j
=================

Java용 마이피플 봇 API 테스트용 예제 소스입니다.

마이피플에서 제공하는 REST API를 이용한 PHP용 sample을 Java용으로 구현되었습니다.
PHP sample [http://dna.daum.net/apis/mypeople/tutorial](http://dna.daum.net/apis/mypeople/tutorial)

Spring 프레임웍을 사용하여 작업되었습니다.
http 통신은 [Httpclient4.x](http://hc.apache.org/httpcomponents-client-4.3.x/index.html) 라이브러리를 사용했고
JSON 파싱은 [JSON](https://github.com/douglascrockford/JSON-java) 을 사용했습니다.

[MyPeopleController](https://github.com/jeedy/DaumMypeopleBot4j/blob/master/src/jee/mypeoplebot/controller/MyPeopleController.java)를 보시면 됩니다.

[MyPeopleController](https://github.com/jeedy/DaumMypeopleBot4j/blob/master/src/jee/mypeoplebot/controller/MyPeopleController.java)에 발급 받은 봇 API key 값을 넣는 static 변수(MYPEOPLE_BOT_APIKEY)가 있습니다. MYPEOPLE_BOT_APIKEY 값을 반드시 입력 후 테스트 하셔야 합니다.
