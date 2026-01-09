package com.uniConnect.shop.service;

import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.shop.dto.CartDto;
import com.uniConnect.shop.entity.Cart;
import com.uniConnect.shop.entity.CartItem;
import com.uniConnect.shop.entity.Product;
import com.uniConnect.shop.repository.CartItemRepository;
import com.uniConnect.shop.repository.CartRepository;
import com.uniConnect.shop.repository.ProductRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final StudentOrgRepository studentOrgRepository;

    /**
     * 장바구니 조회 (학생단체별)
     */
    @Transactional(readOnly = true)
    public CartDto.CartResponse getCart(Long studentOrgId) {
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Cart cart = cartRepository.findByStudentOrgIdWithItems(studentOrgId)
                .orElseGet(() -> createNewCart(studentOrg));

        return convertToCartResponse(cart);
    }

    /**
     * 장바구니에 상품 추가
     */
    public CartDto.CartResponse addItemToCart(Long studentOrgId, CartDto.AddToCartRequest request) {
        StudentOrg studentOrg = studentOrgRepository.findById(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Product product = productRepository.findByIdWithCompany(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Cart cart = cartRepository.findByStudentOrgStudentOrgId(studentOrgId)
                .orElseGet(() -> createNewCart(studentOrg));

        // 기존 항목 확인
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 수량 증가
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            log.info("[장바구니 상품 수량 증가] StudentOrgId={}, ProductId={}, 새로운수량={}",
                    studentOrgId, product.getProductId(), existingItem.getQuantity());
        } else {
            // 새로운 항목 추가
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartRepository.save(cart);
            log.info("[장바구니에 상품 추가] StudentOrgId={}, ProductId={}, 수량={}",
                    studentOrgId, product.getProductId(), request.getQuantity());
        }

        return convertToCartResponse(cart);
    }

    /**
     * 장바구니에서 상품 제거
     */
    public CartDto.CartResponse removeItemFromCart(Long studentOrgId, Long cartItemId) {
        Cart cart = cartRepository.findByStudentOrgStudentOrgId(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);

        log.info("[장바구니에서 상품 제거] StudentOrgId={}, CartItemId={}", studentOrgId, cartItemId);

        return convertToCartResponse(cart);
    }

    /**
     * 장바구니 항목 수량 업데이트
     */
    public CartDto.CartResponse updateItemQuantity(Long studentOrgId, Long cartItemId, Integer quantity) {
        if (quantity < 1) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Cart cart = cartRepository.findByStudentOrgStudentOrgId(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        log.info("[장바구니 상품 수량 수정] StudentOrgId={}, CartItemId={}, 새로운수량={}",
                studentOrgId, cartItemId, quantity);

        return convertToCartResponse(cart);
    }

    /**
     * 장바구니 전체 초기화
     */
    public void clearCart(Long studentOrgId) {
        Cart cart = cartRepository.findByStudentOrgStudentOrgId(studentOrgId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("[장바구니 전체 초기화] StudentOrgId={}", studentOrgId);
    }

    /**
     * 새 Cart 생성
     */
    private Cart createNewCart(StudentOrg studentOrg) {
        Cart cart = Cart.builder()
                .studentOrg(studentOrg)
                .build();
        return cartRepository.save(cart);
    }

    /**
     * DTO 변환
     */
    private CartDto.CartResponse convertToCartResponse(Cart cart) {
        List<CartDto.CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartDto.CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProduct().getProductId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CartDto.CartResponse.builder()
                .cartId(cart.getCartId())
                .studentOrgId(cart.getStudentOrg().getStudentOrgId())
                .items(items)
                .totalAmount(cart.getTotalAmount())
                .totalItemCount(cart.getTotalItemCount())
                .build();
    }
}