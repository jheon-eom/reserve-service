# marketboro-test
마켓보로 사전과제 테스트 레포지토리입니다.

## API 목록
1. 회원별 적립금 합계 조회 <br>
요청 예시 - GET /api/v1/member/{id}/point  <br>
응답 예시 - 200 OK <br>
{
  "memberId": 1,
  "totalPoint": 1500
}

2. 회원별 적립금 적립/사용 내역 조회 <br>
요청 예시 - GET /api/v1/member/{id}/point/history <br>
응답 예시 - 200 OK <br>
{
  "memberId": 1,
  "page": 1,
  "pageSize": 10,
  "totalPages": 3,
  "totalElements": 23,
  "pointsHistory": [
    {
      "paymentId": 1,
      "type": SAVE,
      "amount": 500,
      "createdAt": "2023-07-17T10:30:00Z"
    },
    {
      "paymentId": 2,
      "type": USE,
      "amount": 200,
      "createdAt": "2023-07-16T14:45:00Z"
    }
  ]
}

3. 회원별 적립금 적립 <br>
요청 예시 - POST /api/v1/member/{id}/point/desposit <br>
{
  "paymentId": 1,
  "amount": 500
} <br>
응답 예시 - 201 OK <br>
{
  "memberId": 1,
  "totalPoint": 1000
}

4. 회원별 적립금 사용 <br>
요청 예시 - POST /api/v1/member/{id}/point/withdraw <br>
{
  "amount": 500
} <br>
응답 예시 201 OK <br>
{
  "memberId": 1,
  "totalPoint": 800
}
