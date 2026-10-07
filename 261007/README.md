# 261007 / 공부내용 — Spring Boot 프로젝트 코드와 디버깅

2026년 10월 7일에 직접 확인한 문제와 이 저장소의 Week 5 복습 코드를 공부하기 좋게 모았다. **먼저 앱을 실행하고, 계산 예제를 따라가고, 게시판 구조를 읽은 뒤, 오류 원인과 용어를 복습**한다. 프로젝트의 Java·HTML 원본은 상위 폴더의 [src](../src)에 있다.

## 오늘 만든 공부 자료

| 읽는 순서 | 제목 | 무엇을 공부하나? |
|---|---|---|
| 1 | [공부내용 — 프로젝트 코드 해설](공부내용-프로젝트-코드해설.md) | 각 파일의 역할, URL→메서드→결과, 더하기, 게시판 CRUD, REST, 테스트와 중단점 |
| 2 | [공부내용 — Spring·Java 용어 암기](공부내용-스프링-용어암기.md) | Map·Model·stream.map의 차이, MVC, 요청/응답, 어노테이션, JPA, IntelliJ와 1분 암기 카드 |
| 3 | [공부내용 — IntelliJ·Gradle 오류 보고서](공부내용-IntelliJ-Gradle-오류보고서.md) | 실행 버튼이 없던 증상, 로그로 원인 찾기, 데몬 권한 문제, 8081 포트 충돌과 검증 방법 |

[프로젝트 전체 안내와 기존 개념 노트](../README.md)도 함께 읽을 수 있다.

## 실제로 확인한 결과와 아직 해볼 일

| 단계 | 오늘 확인한 내용 |
|---|---|
| Gradle 불러오기 | IntelliJ에서 BUILD SUCCESSFUL, Gradle Tasks 표시, main 왼쪽 실행 버튼 표시 |
| 서버 구분 | 처음 8081의 INDEX!는 예전 spring-practice 서버였다. 그 서버를 종료한 뒤 새 프로젝트 응답을 확인 |
| 첫 화면 | 새 프로젝트의 첫 화면 제목은 “Spring 5주차 복습” |
| 계산 | /test는 OK, /practice/add?a=2&b=3은 5, /practice/multiply?a=2&b=3과 REST 곱하기는 각각 6 |
| 게시판 조회 | /boards에는 “게시글 목록”, 글이 없을 때 /api/boards는 빈 배열 [] |
| 직접 더 해볼 일 | 게시글 하나를 직접 작성·수정·삭제하고 화면과 REST 조회 결과를 비교하기 |

브라우저 주소는 앱을 실행한 컴퓨터에서 **http://localhost:8081/**이다. GitHub의 링크를 클릭한다고 그 서버가 인터넷에 공개되는 것은 아니다. Run 창의 Started Week5Application을 확인한 뒤 내 브라우저에서 접속한다. H2 메모리 DB를 사용하므로 앱을 껐다 켜면 연습 글이 지워진다.

## 30분 복습 코스

1. **5분 — 실행:** [Week5Application.java](../src/main/java/com/example/week5/Week5Application.java)의 main 옆 실행 버튼을 누르고 첫 화면을 연다.
2. **10분 — 더하기:** [PracticeController.java](../src/main/java/com/example/week5/controller/PracticeController.java)에서 add()를 찾고 [result.html](../src/main/resources/templates/practice/result.html)에서 결과가 표시되는 부분을 찾는다. 주소의 a, b를 바꿔 결과를 예상한다.
3. **10분 — 게시판:** /boards에서 새 글을 만들고 [BoardController.java](../src/main/java/com/example/week5/controller/BoardController.java) → [BoardServiceImpl.java](../src/main/java/com/example/week5/service/impl/BoardServiceImpl.java) → [BoardRepository.java](../src/main/java/com/example/week5/repository/BoardRepository.java)를 순서대로 연다.
4. **5분 — 말로 설명:** 아래 질문에 답을 보지 않고 한 문장씩 말한다.

| 질문 | 핵심 답 |
|---|---|
| Controller, Service, Repository의 역할은? | 요청·응답, 처리 규칙, DB 접근 |
| Map과 Model의 차이는? | Java 키·값 자료구조, HTML에 건넬 데이터를 담는 Spring 객체 |
| 화면에 5가 어떻게 나타나나? | a+b → Model의 result → Thymeleaf의 result 표시 |
| BUILD SUCCESSFUL이면 웹 서버도 켜진 것인가? | 아니다. 실행 로그와 HTTP 응답을 따로 확인한다. |

오늘 공부의 목표는 **코드를 외워 적는 것보다 입력→처리→출력을 실제 파일에서 찾아 설명하는 것**이다.
