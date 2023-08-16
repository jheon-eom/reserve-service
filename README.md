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
![image](https://github.com/jheon-eom/marketboro-test/assets/79975547/d219de40-e953-4a94-8cd5-14b8c5a899d0)
UPDATE, DELETE 시 데이터의 보존성, 동시성 문제를 고려하여 <br>
적립, 사용, 취소에 대해서 모두 데이터를 INSERT하는 방식으로 구현하였습니다. <br>
<br>
POINT_ID 기준 <br>
1-2번 2번에 걸쳐서 총 50P를 적립하였습니다. <br>
3-4번 한 트랜잭션에서 40P를 사용하였고 1번의 30P, 2번의 10P를 나눠서 사용하였습니다. <br>
5-6번 40P 사용을 취소하였습니다.<br>
7-8번 다시 40P 사용 시 취소되었던 1~2번 포인트를 사용하였습니다. <br>

## 동시성 문제
포인트를 사용하거나 취소할 경우 동시성 문제가 특히 주의해야할 부분이라고 생각했습니다. <br>
(사용 또는 취소 시에 트랜잭션이 커밋되지 않은 시점에서 다른 트랜잭션이 커밋되지 않은 데이터를 읽어들여서 <br>
데이터를 수정할 경우 포인트가 음수가 나올 수도 있다고 생각되었습니다.) <br>
<br>
이 부분을 염두하여 RDB에 LOCK 전용 테이블을 설계하였습니다. <br>
포인트 사용, 취소 시에 한 계정이 여러 번 같은 트랜잭션을 실행할 수 없게 락을 획득합니다. <br>
락을 획득한 도중 같은 계정의 다른 트랜잭션이 해당 로직을 실행할 경우 락 테이블 체크에 의해 예외를 반환받게됩니다. <br>
먼저 실행 중인 트랜잭션이 모두 마치고 락을 놓게 되면 다음 트랜잭션이 작업을 수행할 수 있습니다.

## 고려 사항
1. 마이크로서비스 아키텍처를 적용하여 포인트 도메인을 분리한 상황임을 가정하고 개발하였습니다. <br>
-> 주로 결제 서비스에서 적립금 서비스를 호출하였을 것으로 예상하였습니다. <br>
-> HTTP 요청 파라미터의 memberId는 접속 중인 사용자임이 검증된 상태일 것입니다.

