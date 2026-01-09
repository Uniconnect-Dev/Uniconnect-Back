package com.uniConnect.shop.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id", nullable = false)
    private StudentOrg studentOrg;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    /**
     * 장바구니 총 금액 계산
     */
    public Integer getTotalAmount() {
        return items.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * 장바구니 총 아이템 수
     */
    public Integer getTotalItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}