package com.uniConnect.shop.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.company.entity.Company;
import com.uniConnect.shop.enums.ProductCategory;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Integer price;   // 단위: 원

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @Column(name = "detail_image_url", columnDefinition = "text")
    private String detailImageUrl;

    @Column(name = "short_description", length = 300)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;
}
