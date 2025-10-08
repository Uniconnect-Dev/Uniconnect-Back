package com.uniConnect.collaboration.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "product_infos")
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_info_id")
    private Long productInfoId;

    // info
    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "logo_url", columnDefinition = "text")
    private String logoUrl;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "tracking_no", length = 50)
    private String trackingNo;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;
}
