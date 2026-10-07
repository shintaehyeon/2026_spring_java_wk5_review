# DTO 이해하기 — 요청과 응답 데이터의 모양

**한 문장 정의:** DTO(Data Transfer Object)는 **데이터를 주고받기 위해 필요한 값만 묶은 객체**다. DTO는 스프링의 필수 문법이나 어노테이션이 아니다. 그렇지만 컨트롤러가 받는 값, 밖으로 보내는 값, DB에 저장하는 값을 구분하려면 꼭 알아야 한다.

## 1. 먼저 세 가지를 구분하자

| 이름 | 하는 일 | 이 프로젝트의 예 |
|---|---|---|
| Entity | DB에 저장·조회할 데이터의 모양 | [`Board`](../src/main/java/com/example/week5/entity/Board.java): `id`, `title`, `content` |
| 요청 DTO | 클라이언트가 보내는 데이터의 모양 | [`BoardRequest`](../src/main/java/com/example/week5/controller/BoardRestController.java): `title`, `content` |
| 응답 DTO | 클라이언트에 보여줄 데이터의 모양 | [`BoardResponse`](../src/main/java/com/example/week5/controller/BoardRestController.java): `id`, `title`, `content` |

지금은 세 객체의 필드가 비슷하다. **역할이 달라서** 따로 쓴다. 예를 들어 나중에 DB 엔티티에 내부 메모나 관리용 필드가 추가돼도, 응답 DTO에 넣지 않으면 API 응답에는 나오지 않는다. 반대로 새 글을 쓰는 사용자가 DB의 `id`까지 입력할 필요는 없으므로 요청 DTO에는 `id`가 없다.

> 헷갈리기 쉬운 말: `Model`은 서버의 값을 Thymeleaf **HTML 화면에 전달**할 때 쓰는 스프링 객체다. DTO는 요청·응답 등에서 **전달할 데이터의 형태**를 정한 객체다. `BoardResponse`는 화면의 `Model`이 아니다.

## 2. 실제 코드에서 데이터가 움직이는 순서

이 프로젝트에서 **POST `/api/boards`**로 글을 만들 때:

```text
클라이언트 JSON {"title":"첫 글","content":"복습 중"}
        ↓ @RequestBody
BoardRequest(title, content)              ← 요청 DTO
        ↓ request.title(), request.content()
BoardService.create(...) → Board Entity → BoardRepository → DB
        ↓ 저장된 Board의 id·title·content
BoardResponse.from(board)                 ← 응답 DTO
        ↓ @RestController가 응답 본문으로 보냄
클라이언트 JSON {"id":1,"title":"첫 글","content":"복습 중"}
```

여기서 JSON의 `1`은 **예시 번호**다. 실제 번호는 DB 저장 결과에 따라 달라진다. 현재 코드는 [`BoardRestController.java`](../src/main/java/com/example/week5/controller/BoardRestController.java)에 두 DTO를 `record`로 정의했다.

```java
public record BoardRequest(String title, String content) { }

public record BoardResponse(Long id, String title, String content) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(board.getId(), board.getTitle(), board.getContent());
    }
}
```

`record`는 이런 값 묶음을 짧게 선언하는 Java 문법이다. `BoardRequest`를 쓰면 `request.title()`처럼 값을 읽는다. `BoardResponse.from(board)`는 **Entity에서 필요한 값을 골라 응답 DTO로 옮기는 변환 메서드**다. DTO는 꼭 `record`로 만들어야 하는 것은 아니며 일반 Java 클래스로도 만들 수 있다. 지금 DTO들이 `BoardRestController` 안에 있는 것은 이 작은 연습 프로젝트의 구성이지, 반드시 따라야 하는 폴더 규칙은 아니다.

## 3. 왜 Entity를 그대로 주고받지 않을까?

1. **입력과 저장을 구분:** 글 작성 요청에는 제목·내용만 필요하지만, DB의 `id`는 저장할 때 정해진다.
2. **응답 내용을 선택:** 외부에 보여줄 필드만 응답 DTO에 넣는다. 내부 필드가 Entity에 생겨도 자동으로 공개되지 않게 설계할 수 있다.
3. **변경을 분리:** DB 구조가 바뀌어도 API가 받거나 보내는 모양을 따로 유지할 수 있다.

이 말이 **모든 메서드에 DTO가 의무**라는 뜻은 아니다. 이 프로젝트의 HTML 페이지 컨트롤러는 `@RequestParam`으로 입력을 받고 `Model`에 `Board`를 담아 템플릿으로 보낸다. JSON을 주고받는 REST 컨트롤러에서는 `BoardRequest`와 `BoardResponse`를 사용한다. 두 방식을 비교해서 읽는 것이 이번 공부의 핵심이다.

## 4. 코드에서 바로 찾아보기

| 찾아볼 곳 | 확인할 코드 | 뜻 |
|---|---|---|
| [`BoardRestController.create()`](../src/main/java/com/example/week5/controller/BoardRestController.java) | `@RequestBody BoardRequest request` | JSON 본문을 요청 DTO로 받음 |
| 같은 메서드 | `boardService.create(request.title(), request.content())` | 받은 값을 서비스에 전달 |
| 같은 메서드 | `BoardResponse.from(board)` | 저장된 Entity를 응답 DTO로 바꿈 |
| [`BoardRestController.list()`](../src/main/java/com/example/week5/controller/BoardRestController.java) | `.map(BoardResponse::from).toList()` | 여러 Entity를 하나씩 응답 DTO로 바꿈 |
| [`BoardController.create()`](../src/main/java/com/example/week5/controller/BoardController.java) | `@RequestParam String title` | HTML 폼은 DTO 없이 파라미터를 직접 받는 예 |

**주의:** 여기서 `.map(...)`은 목록의 각 항목을 바꾸는 메서드다. `Map<String, Object>`처럼 키와 값을 저장하는 Java 자료구조와 다르다.

## 5. 스스로 답해보기

1. `BoardRequest`에 `id`가 없는 이유는? → 글을 새로 만들 때 ID는 사용자가 보내는 값이 아니라 DB 저장 과정에서 생기는 값이기 때문이다.
2. `BoardResponse`에 `id`가 있는 이유는? → 클라이언트가 만들어진 글을 식별하고 `/api/boards/{id}`로 다시 조회할 수 있게 하기 위해서다.
3. `Board`와 `BoardResponse`가 같은 객체인가? → 아니다. 전자는 DB에 연결된 Entity, 후자는 API로 보낼 데이터를 담은 DTO다.
4. `@RequestBody`와 DTO는 같은 것인가? → 아니다. `@RequestBody`는 요청 본문을 읽으라는 스프링 표시이고, DTO는 그 데이터를 담을 Java 타입이다.
5. DTO를 사용하면 입력 검사가 자동으로 끝나는가? → 아니다. **검증은 별도 작업**이다. 현재 `BoardRequest`에는 입력 검증 규칙이 없다.

**외울 문장:** “요청 DTO로 받고 → Entity로 저장하고 → 응답 DTO로 보내며, HTML 화면에 값을 넘길 때는 Model을 쓴다.”
