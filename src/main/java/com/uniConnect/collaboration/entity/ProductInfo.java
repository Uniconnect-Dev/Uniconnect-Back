package com.uniConnect.collaboration.entity;

import com.uniConnect.collaboration.enums.UploaderType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_infos")
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_info_id")
    private Long productInfoId;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(columnDefinition = "text")
    private String logoUrl;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "is_shipped")
    private Boolean isShipped;

    @Column(name = "tracking_no", length = 50)
    private String trackingNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    private Collaboration collaboration;

    @Enumerated(EnumType.STRING)
    @Column(name = "provided_by", length = 20)
    private UploaderType providedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isShipped == null) {
            this.isShipped = false;
        }
    }
}
