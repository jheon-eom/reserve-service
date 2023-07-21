# marketboro-test
안녕하세요. 마켓보로 백엔드 개발자로 지원한 엄종헌입니다. <br>
마켓보로 사전과제 테스트 레포지토리입니다.

## 사용 기술
Java 17 <br>
Spring Boot 3.0.8 <br>
Spring Data JPA <br>
H2 Inmemory DataBase <br>
Junit5 <br>

## API Document
Gitbook을 통해 API 스펙을 작성하였습니다. <br>
https://eomprogrammers-organization.gitbook.io/untitled/

## DB 테이블 설계
![image](https://github.com/jheon-eom/marketboro-test/assets/79975547/920ab451-4bb0-4caa-96b6-ffa106046087)
![image](https://github.com/jheon-eom/marketboro-test/assets/79975547/0b801fe6-ad22-4418-8083-645173b17e83)

## 비즈니스 로직 설계
![image](https://github.com/jheon-eom/marketboro-test/assets/79975547/fec3ae05-8404-4c82-86c1-5ad61c7b56c8)
UPDATE, DELETE 시 데이터의 보존성, 동시성 문제를 고려하여 <br>
적립, 사용, 취소에 대해서 모두 데이터를 INSERT하는 방식으로 구현하였습니다. <br>
<br>
POINT_ID 기준 <br>
1~2번 2번에 걸쳐서 총 50P를 적립하였습니다. <br>
3~4번 한 트랜잭션에서 40P를 사용하였고 1번의 30P, 2번의 10P를 나눠서 사용하였습니다. <br>
5~6번 40P 사용을 취소하였습니다.<br>
7~8번 다시 40P 사용 시 취소되었던 1~2번 포인트를 사용하였습니다. <br>
