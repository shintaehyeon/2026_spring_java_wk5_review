# IntelliJ에서 실행 버튼이 보이지 않았던 Gradle 오류 보고서

> 기록일: 2026-10-07
> 대상: `2026_spring_java_wk5_review` (Spring Boot 4.1.1, Java 17, Gradle 9.7.1)

## 1. 결과부터 확인

IntelliJ에서 Java 17을 설정했지만 `Week5Application.java`의 `main` 함수 왼쪽에 실행 버튼(▶)이 보이지 않았다. **Java 코드의 `main` 함수가 빠진 것이 아니라 Gradle 프로젝트 불러오기(동기화)가 실패한 것**이 원인이었다.

접근 권한이 제한된 환경에서 시작한 Gradle 백그라운드 프로세스를 IntelliJ가 재사용하고 있었다. 해당 프로세스를 종료한 뒤 Gradle을 다시 불러오자, IntelliJ 화면에서 **`BUILD SUCCESSFUL in 11s`**, Gradle의 `Tasks`·`Dependencies`, `main` 옆 초록 ▶가 확인됐다.

**확인 범위:** Gradle 성공 화면을 본 시점에는 프로젝트 불러오기만 확인됐다. 이후 8081의 이전 서버를 종료하고 새 프로젝트의 HTTP 응답을 직접 확인했다. IntelliJ Run 창의 시작 문구 자체는 별도 기록으로 남기지 않았다.

## 2. 무엇이 보였나: 증상과 증거

| 순서 | 화면 또는 로그에서 확인한 사실 | 의미 |
|---|---|---|
| 1 | `Project JDK is not defined` | 처음에는 이 프로젝트에 Java SDK가 지정되지 않았다. |
| 2 | Java 17 설정 후에도 `Cannot create directory '.../.gradle/9.7.1/fileHashes'` | SDK 설정만으로는 해결되지 않았다. Gradle이 작업 파일을 만들지 못했다. |
| 3 | 필요한 폴더를 만든 뒤에도 `java.io.IOException: Operation not permitted` | 단순히 폴더가 없어서 생긴 오류가 아니었다. 실행 프로세스의 접근 권한을 살펴봐야 했다. |
| 4 | Gradle 기록에서 같은 프로세스가 확인 작업과 IntelliJ의 작업을 모두 처리 | 제한된 환경에서 시작된 프로세스를 IntelliJ가 재사용했다는 근거다. |
| 5 | 그 프로세스를 종료하고 IntelliJ에서 다시 불러온 뒤 `BUILD SUCCESSFUL in 11s` | Gradle 불러오기가 완료되고 실행 버튼이 나타났다. |

당시 `~/.gradle/daemon/9.7.1/daemon-73658.out.log`에는 확인 작업의 환경 정보(`CODEX_SANDBOX`)와, **같은 프로세스가 IntelliJ 프로젝트 경로의 빌드 요청을 처리하다 `Operation not permitted`를 낸 기록**이 있었다. 이 프로세스의 종료 기록도 확인했다. `73658`은 이번 사례의 프로세스 번호일 뿐이므로 다른 날 그대로 사용하면 안 된다.

## 3. 왜 이런 일이 생겼나

Gradle은 빌드 속도를 높이기 위해 **데몬(daemon)**이라는 백그라운드 프로세스를 계속 띄워둘 수 있다. IntelliJ도 Gradle 프로젝트를 불러올 때 이 프로세스를 사용한다.

```text
IntelliJ에서 프로젝트 열기
  → Gradle 동기화 요청
  → 기존 Gradle 데몬 재사용
  → 데몬의 파일 접근이 거부됨
  → 동기화 실패
  → IntelliJ가 Java 실행 대상을 인식하지 못해 ▶가 나타나지 않음
```

이번에는 확인용 Gradle 데몬이 **접근 권한이 제한된 Codex 작업 환경**에서 먼저 시작됐다. 이후 IntelliJ가 같은 데몬을 재사용해도 데몬 자체의 제한이 사라지지 않는다. 그래서 프로젝트 폴더가 존재하고 Java 17이 설치되어 있어도 파일 생성·잠금 작업에서 `Operation not permitted`가 났다.

이것은 **Spring 컨트롤러 코드의 오류가 아니라 빌드 도구의 실행 환경 문제**다. 다만 원인을 확정할 때는 추측만 하지 않고, 같은 프로세스가 두 작업을 처리한 기록과 종료 후 동기화 성공을 함께 확인해야 한다.

## 4. 실제로 한 조치

1. 프로젝트 SDK를 Java 17로 지정했다.
2. Gradle 오류가 가리킨 폴더와 접근 권한을 확인했다.
3. 폴더를 만들어도 오류가 바뀌지 않는 것을 확인하고 Gradle 데몬 로그를 조사했다.
4. 제한된 환경에서 시작된 **이번 사례의 데몬만** 종료했다.
5. IntelliJ에서 Gradle을 다시 불러왔다.
6. `BUILD SUCCESSFUL`, Gradle 프로젝트 목록, `main` 왼쪽의 ▶를 확인했다.

**재발하면:** 먼저 IntelliJ의 *Build* 창에서 최신 오류를 읽는다. 같은 접근 권한 오류이고 제한된 Gradle 데몬의 재사용이 확인될 때만 데몬을 중지한 뒤 Gradle 새로고침을 한다. 무조건 SDK를 재설치하거나 프로젝트를 지울 필요는 없다.

## 5. 헷갈리기 쉬웠던 별개 오류

`build.gradle` 파일을 열어 둔 채 상단의 **Current File ▶**를 눌렀을 때 `Unknown command-line option '-b'`가 나왔다. 이것은 Spring 앱의 `main` 함수를 실행한 것이 아니라 **열려 있던 Gradle 파일을 실행하려 한 것**이다. Gradle 동기화의 `Operation not permitted`와는 다른 문제다.

앱을 실행할 때는 `Week5Application.java`를 열고 아래 줄 **왼쪽 여백의 초록 ▶**를 누른다.

```java
public static void main(String[] args)
```

창 위쪽 Git 표시 옆의 `main`은 **Git 브랜치 이름**이다. Java의 `main()` 함수와 관계없다.

## 6. 지금 다음으로 할 일: 성공을 세 단계로 확인

| 단계 | 무엇을 확인할까? | 이번 사례의 상태 |
|---|---|---|
| ① 프로젝트 불러오기 | `BUILD SUCCESSFUL`, Gradle 목록, `main` 옆 ▶ | **화면에서 확인됨** |
| ② 앱 실행 | Week5Application 실행 | 새 프로젝트의 HTTP 응답으로 실행 중임을 확인. Run 창의 시작 문구는 별도 기록 없음 |
| ③ 웹 동작 | 첫 화면, /test, /boards, 더하기·곱하기 주소 | 각 주소의 정상 응답 확인. 글 작성·수정·삭제는 별도 직접 실습 필요 |

②에서 **8081 포트를 이미 사용 중**이라는 오류가 나오면 이전에 실행한 `spring-practice`를 먼저 중지하고 다시 실행한다. `BUILD SUCCESSFUL`만 보고 웹 서버까지 실행됐다고 판단하지 않는다.

### 같은 날 확인한 두 번째 문제: 예전 서버가 8081을 사용

Gradle 오류가 해결된 뒤에도 브라우저 첫 화면에 INDEX!가 보였다. 새 프로젝트의 첫 화면 제목은 Spring 5주차 복습이므로, 그 화면은 새 프로젝트의 결과가 아니었다.

- 8081 응답은 예전 spring-practice 프로젝트의 index.html이었고, 그 주소의 /test와 /boards는 404였다.
- 8081을 사용 중인 Java 프로세스의 작업 폴더가 spring-practice임을 확인했다. IntelliJ 창이 닫혀 있어도 서버 프로세스는 남아 있을 수 있다.
- 그 예전 서버만 종료한 뒤 8081이 비어 있음을 확인하고 새 Week5Application을 실행했다.
- 이후 첫 화면의 제목은 Spring 5주차 복습, /test 응답은 OK, /practice/add?a=2&b=3 결과는 5, /boards는 게시글 목록, /api/boards는 빈 배열이었다.

**교훈:** BUILD SUCCESSFUL과 서버 실행은 다르다. URL이 열려도 예상한 앱인지 첫 화면 제목·요청 주소·실행 프로세스를 비교해야 한다.

## 7. 다음에도 쓸 수 있는 디버깅 방법

1. **증상을 적는다.** “▶가 없다.”
2. **가장 먼저 실패한 단계를 찾는다.** 이번에는 Java 실행 이전의 Gradle 동기화였다.
3. **오류 문구를 그대로 읽는다.** `Operation not permitted`는 코드 문법보다 파일 접근·실행 환경을 먼저 의심하게 한다.
4. **가설을 하나 세우고 증거를 찾는다.** 폴더 상태, 오류 시각, Gradle 데몬 로그, 프로세스 재사용 여부를 비교했다.
5. **한 번에 하나만 바꾸고 다시 확인한다.** 제한된 데몬을 종료하고 동기화를 다시 시도했다.
6. **확인한 범위만 성공이라고 기록한다.** 빌드 성공, 앱 실행, 웹 응답은 서로 다른 단계다.

오류별로 살펴볼 위치도 다르다: `cannot find symbol`은 Java 코드·import, `Operation not permitted`는 접근 권한·프로세스 환경, `Port 8081 was already in use`는 실행 중인 다른 서버, `404`는 서버 실행 후 요청 주소와 컨트롤러 매핑을 먼저 확인한다.

---

이 보고서는 2026-10-07의 학습 기록이다. 오류 원인과 확인 범위를 구분해 적었으며, 앱의 Java 기능 코드를 바꾸지는 않았다.
