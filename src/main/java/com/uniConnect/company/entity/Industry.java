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

    @Column(name = "name", length = 50)
    private String name;
}
