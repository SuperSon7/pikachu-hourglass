# 피카츄 모래시계 ⚡

**Java로 만든** 간단한 포켓몬 모래시계 웹페이지입니다. JavaScript를 사용하지 않습니다.

Java 표준 라이브러리의 `HttpServer`가 시간 선택, 카운트다운, 일시정지, 초기화를 처리하고 HTML을 생성합니다. 실행 중에는 브라우저가 1초마다 페이지를 새로고침하여 남은 시간을 표시합니다.

## 실행

JDK 17 이상을 설치한 뒤 저장소 루트에서 실행하세요.

```sh
java src/Main.java
```

브라우저에서 http://localhost:8080 에 접속하세요. 종료는 `Ctrl+C`입니다.

포트를 바꾸려면 `java src/Main.java 9090`처럼 지정하세요. 별도 라이브러리나 빌드 도구는 필요 없습니다.

## 기능

- 1분 · 3분 · 5분 · 10분 선택
- 시작, 일시정지, 이어하기, 초기화
- 남은 시간에 따라 변하는 모래시계
- 브라우저 쿠키별 독립 타이머

## 파일

- `src/Main.java`: 웹서버 및 타이머 로직
- `public/index.html`: 서버에서 값을 채워 보내는 HTML/CSS 템플릿
- `public/pikachu.png`: 피카츄 이미지

HTML 파일을 직접 열지 말고 Java 서버에 접속하세요. 타이머 상태는 서버 메모리에 저장되어 서버를 종료하면 초기화됩니다. 개인 로컬 사용을 위한 간단한 서버입니다.

## 이미지 출처

피카츄 스프라이트: [PokeAPI/sprites](https://github.com/PokeAPI/sprites/blob/master/sprites/pokemon/25.png).

Pokémon 및 관련 캐릭터의 권리는 해당 권리자에게 있습니다. 이 프로젝트는 비공식 팬 프로젝트입니다.
