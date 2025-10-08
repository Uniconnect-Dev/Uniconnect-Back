package com.uniConnect.studentorg.entity;

import com.uniConnect.studentorg.enums.HashtagCategory;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "hashtags")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private HashtagCategory category;

    @Column(name = "name", length = 100)
    private String name;
}
