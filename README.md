# 랜덤채팅
안드로이드. 두번째 작품

# 구현전 예제정리
- 유튜브 API : https://gist.github.com/byungop/ca71f5d4269cbf2174ea637db259f08a
- 네이버 뉴스 API : https://gist.github.com/byungop/d390b77e06d944b90f25c586c2d6c7ba
- SMS메시지 송수신 : https://gist.github.com/byungop/805b304f694fb6922f9821ce31701bb9
- TTS 기능 : https://gist.github.com/byungop/ab624f1745e55070d10aa4be7af296ad
- SQLite CRUD : https://gist.github.com/byungop/42c9b13e33b021194cade4df4e280acd
- 커스텀 토스트 https://gist.github.com/byungop/0a2845aa80fe297cd45ac39795d40f37


# 부연설명
: 랜덤채팅의 경우 TCP 채팅서버를 두가지 방법으로 만들어보았습니다.
해당 레포지토리의 서버는 Node.js Socket.io 라이브러리를 활용하여 만들었고
별개로 Java로도 TCP 서버를 구현해 보았습니다. ( https://gist.github.com/byungop/fc27be9ff7f9523e8444051aa3848f91 )

# 구현한 기능
1. 로그인 (파이어베이스UI)
2. 회원관리 (volley 라이브러리, MySQL 저장)
3. 채팅 (Node.JS 서버, Socket IO 라이브러리)
