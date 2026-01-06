package com.uniConnect.common.service.impl;

import com.uniConnect.common.service.EmailService;
import com.uniConnect.payment.entity.RefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * 이메일 발송 서비스 구현체
 * Thymeleaf 템플릿을 사용하여 HTML 형식의 이메일을 발송합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender; //spring.mail.host,port 설정 있어야 autowire
    private final TemplateEngine templateEngine;

    //보내는 email
    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.email.company-name:uniConnect}")
    private String companyName;

//    //연락할 email
//    @Value("${app.email.support-email")
//    private String supportEmail;

//    /**
//     * 세금계산서/영수증 발급 이메일 발송
//     */
//    @Override
//    public void sendInvoiceEmail(String recipientEmail, Invoice invoice, String pdfUrl) {
//        try {
//            log.info("[이메일 발송] 세금계산서 - Recipient: {}, Invoice ID: {}",
//                    recipientEmail, invoice.getInvoiceId());
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            // 기본 정보 설정
//            helper.setFrom(fromEmail);
//            helper.setTo(recipientEmail);
//            helper.setSubject("[" + companyName + "] 세금계산서/영수증 발급 안내");
//
//            // thymeleaf 템플릿 데이터 설정
//            Context context = new Context();
//            context.setVariable("invoice", invoice);
//            context.setVariable("pdfUrl", pdfUrl);
//            context.setVariable("companyName", companyName);
//            context.setVariable("issuedAt",
//                    invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
//            context.setVariable("amount", formatCurrency(invoice.getPayment().getAmount()));
//
//            // HTML 템플릿 렌더링
//            String htmlContent = templateEngine.process("email/invoice-email", context);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//
//            log.info("[이메일 발송 성공] 세금계산서 - Recipient: {}", recipientEmail);
//
//        } catch (MessagingException e) {
//            log.error("[이메일 발송 실패] 세금계산서 - Recipient: {}, Error: {}",
//                    recipientEmail, e.getMessage(), e);
//            // 이메일 발송 실패는 비즈니스 로직에 영향을 주지 않도록 예외 처리
//        }
//    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[" + companyName + "] 이메일 인증 코드");
            message.setText(buildVerificationCodeEmailBody(code));

            mailSender.send(message);
            log.info("✉️ 인증 코드 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ 인증 코드 이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    private String buildVerificationCodeEmailBody(String code) {
        return String.format(
                "%s 회원가입을 위한 이메일 인증 코드입니다.\n\n" +
                        "인증 코드: %s\n\n" +
                        "5분 이내에 입력해주세요.\n\n" +
                        "감사합니다.",
                companyName,
                code
        );
    }

    /**
     * 환불 요청 접수 이메일 발송
     */
    @Override
    public void sendRefundRequestConfirmationEmail(String recipientEmail, RefundRequest refundRequest) {
        try {
            log.info("[이메일 발송] 환불 요청 접수 - Recipient: {}, Refund ID: {}",
                    recipientEmail, refundRequest.getRefundId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("[" + companyName + "] 환불 요청이 접수되었습니다");

            Context context = new Context();
            context.setVariable("refund", refundRequest);
            context.setVariable("companyName", companyName);
            context.setVariable("requesterName", refundRequest.getRequesterName());
            context.setVariable("refundAmount", formatCurrency(refundRequest.getRefundAmount()));
            context.setVariable("requestedAt",
                    refundRequest.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
            context.setVariable("reason", refundRequest.getReason());

            String htmlContent = templateEngine.process("email/refund-request-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("[이메일 발송 성공] 환불 요청 접수 - Recipient: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("[이메일 발송 실패] 환불 요청 접수 - Recipient: {}, Error: {}",
                    recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * 환불 거절 이메일 발송
     */
    @Override
    public void sendRefundRejectionEmail(String recipientEmail, RefundRequest refundRequest) {
        try {
            log.info("[이메일 발송] 환불 거절 - Recipient: {}, Refund ID: {}",
                    recipientEmail, refundRequest.getRefundId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("[" + companyName + "] 환불 요청이 거절되었습니다");

            Context context = new Context();
            context.setVariable("refund", refundRequest);
            context.setVariable("companyName", companyName);
            context.setVariable("requesterName", refundRequest.getRequesterName());
            context.setVariable("refundAmount", formatCurrency(refundRequest.getRefundAmount()));
//            context.setVariable("rejectionReason", refundRequest.getRefundRejectionReason());

            String htmlContent = templateEngine.process("email/refund-rejection-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("[이메일 발송 성공] 환불 거절 - Recipient: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("[이메일 발송 실패] 환불 거절 - Recipient: {}, Error: {}",
                    recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * 환불 완료 이메일 발송
     */
    @Override
    public void sendRefundCompletionEmail(String recipientEmail, RefundRequest refundRequest) {
        try {
            log.info("[이메일 발송] 환불 완료 - Recipient: {}, Refund ID: {}",
                    recipientEmail, refundRequest.getRefundId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("[" + companyName + "] 환불이 완료되었습니다");

            Context context = new Context();
            context.setVariable("refund", refundRequest);
            context.setVariable("companyName", companyName);
            context.setVariable("requesterName", refundRequest.getRequesterName());
            context.setVariable("refundAmount", formatCurrency(refundRequest.getRefundAmount()));
            context.setVariable("bankName", refundRequest.getBankName());
//            context.setVariable("completedAt",
//                    refundRequest.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")));
//            context.setVariable("refundTransactionId", refundRequest.getRefundTransactionId());

            String htmlContent = templateEngine.process("email/refund-completion-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("[이메일 발송 성공] 환불 완료 - Recipient: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("[이메일 발송 실패] 환불 완료 - Recipient: {}, Error: {}",
                    recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * 금액 포맷팅 (천단위 쉼표)
     */
    private String formatCurrency(Integer amount) {
        if (amount == null) {
            return "0원";
        }
        return String.format("%,d원", amount);
    }
}