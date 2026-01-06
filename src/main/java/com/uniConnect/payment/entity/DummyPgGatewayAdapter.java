package com.uniConnect.payment.entity;

import com.uniConnect.payment.entity.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 더미 PG 게이트웨이 구현
 * 개발 및 테스트 단계에서 사용되며, 실제 결제 처리는 하지 않습니다.
 */
@Slf4j
@Component
public class DummyPgGatewayAdapter implements PgGatewayAdapter {

    /**
     * 더미 결제 처리
     * - 실제 결제는 하지 않고, 임의의 트랜잭션 ID를 생성하여 반환합니다.
     */
    @Override
    public String processPayment(Long paymentId, Integer amount, PaymentMethod paymentMethod) throws Exception {
        log.info("[더미 결제 처리] Payment ID: {}, Amount: {}, Method Type: {}",
                paymentId, amount, paymentMethod.getType());

        // 결제 검증
        validatePaymentMethod(paymentMethod);

        // 시뮬레이션: 10% 확률로 실패
        if (Math.random() < 0.1) {
            throw new Exception("Simulated payment failure");
        }

        // 거래 ID 생성
        String transactionId = generateTransactionId();

        log.info("[더미 결제 성공] Payment ID: {}, Transaction ID: {}", paymentId, transactionId);

        return transactionId;
    }

    @Override
    public String cancelPayment(String transactionId) throws Exception {
        log.info("[더미 결제 취소] Transaction ID: {}", transactionId);

        if (transactionId == null || transactionId.isEmpty()) {
            throw new Exception("Invalid transaction ID");
        }

        String cancelTransactionId = generateTransactionId();

        log.info("[더미 결제 취소 성공] Original Transaction ID: {}, Cancel Transaction ID: {}",
                transactionId, cancelTransactionId);

        return cancelTransactionId;
    }

    @Override
    public String getTransactionStatus(String transactionId) throws Exception {
        log.info("[더미 거래 조회] Transaction ID: {}", transactionId);

        if (transactionId == null || transactionId.isEmpty()) {
            throw new Exception("Invalid transaction ID");
        }

        // 거래 상태 반환 (SUCCESS, FAILED, PENDING 등)
        return "SUCCESS";
    }


    /**
     * 더미 환불 처리
     * - 실제 환불은 하지 않고, 임의의 환불 거래 ID를 생성하여 반환합니다.
     */
    @Override
    public String processRefund(String transactionId, Integer refundAmount) throws Exception {
        log.info("[더미 환불 처리] Transaction ID: {}, Refund Amount: {}", transactionId, refundAmount);

        // 입력 검증
        if (transactionId == null || transactionId.isEmpty()) {
            log.warn("[환불 처리 실패] 유효하지 않은 거래 ID: {}", transactionId);
            throw new Exception("Invalid transaction ID");
        }

        if (refundAmount == null || refundAmount <= 0) {
            log.warn("[환불 처리 실패] 유효하지 않은 환불 금액: {}", refundAmount);
            throw new Exception("Invalid refund amount");
        }

        // 시뮬레이션: 8% 확률로 환불 실패
        if (Math.random() < 0.08) {
            log.error("[더미 환불 처리 실패] 시뮬레이션된 환불 실패");
            throw new Exception("Simulated refund failure");
        }

        // 환불 거래 ID 생성
        String refundTransactionId = generateRefundTransactionId();

        log.info("[더미 환불 처리 성공] Original Transaction ID: {}, Refund Amount: {}, Refund Transaction ID: {}",
                transactionId, refundAmount, refundTransactionId);

        return refundTransactionId;
    }

    /**
     * 결제 수단 검증
     */
    private void validatePaymentMethod(PaymentMethod method) throws Exception {
        if (method == null) {
            throw new Exception("Payment method is null");
        }

        if (method.getType() == null) {
            throw new Exception("Payment method type is null");
        }

        // 카드 결제인 경우 필수 정보 검증
        if ("CARD".equals(method.getType().toString())) {
            if (method.getCardNumberEnc() == null || method.getCardNumberEnc().isEmpty()) {
                throw new Exception("Card number is missing");
            }
            if (method.getExpiry() == null || method.getExpiry().isEmpty()) {
                throw new Exception("Card expiry is missing");
            }
            if (method.getCvcEnc() == null || method.getCvcEnc().isEmpty()) {
                throw new Exception("Card CVC is missing");
            }
        }
    }

    /**
     * 거래 ID 생성
     */
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 거래 ID 생성
     */
    private String generateRefundTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
