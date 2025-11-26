package com.uniConnect.signature.repository;

import com.uniConnect.signature.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRepository extends JpaRepository<Signature, Long> {
}