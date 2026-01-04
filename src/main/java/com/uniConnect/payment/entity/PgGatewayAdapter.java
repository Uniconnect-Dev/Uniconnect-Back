package com.uniConnect.payment.entity;

/**
 * PG 게이트웨이 어댑터 인터페이스
 * 다양한 결제 서비스(PG사)와의 통신을 담당합니다.
 */
public interface PgGatewayAdapter {

    /**
     * 결제 처리
     * @param paymentId 결제 ID
     * @param amount 결제 금액
     * @param paymentMethod 결제 수단
     * @return 거래 ID (Transaction ID)
     * @throws Exception 결제 실패 시 예외 발생
     */
    String processPayment(Long paymentId, Integer amount, PaymentMethod paymentMethod) throws Exception;

    /**
     * 결제 취소
     * @param transactionId 거래 ID
     * @return 취소 거래 ID
     * @throws Exception 취소 실패 시 예외 발생
     */
    String cancelPayment(String transactionId) throws Exception;

    /**
     * 거래 조회
     * @param transactionId 거래 ID
     * @return 거래 상태 정보
     * @throws Exception 조회 실패 시 예외 발생
     */
    String getTransactionStatus(String transactionId) throws Exception;
}
