package com.uniConnect.company.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "industries")
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_id")
    private Long industryId;

    @Column(name = "industry_name", length = 100)
    private String industryName;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
