package com.uniConnect.shop.repository;

import com.uniConnect.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByStudentOrgStudentOrgId(Long studentOrgId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.studentOrg.studentOrgId = :studentOrgId")
    Optional<Cart> findByStudentOrgIdWithItems(@Param("studentOrgId") Long studentOrgId);
}