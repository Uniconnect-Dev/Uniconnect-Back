package com.uniConnect.collaboration.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "shipping_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 협업에 속한 발송정보인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    /** 발송일자 */
    private LocalDate shippingDate;

    /** 운송장 번호 */
    private String trackingNumber;

    /** 배송 여부 (true = 발송 완료, false = 미발송) */
    private Boolean isShipped;

    /** 메모 또는 비고 (선택사항) */
    private String note;
}
