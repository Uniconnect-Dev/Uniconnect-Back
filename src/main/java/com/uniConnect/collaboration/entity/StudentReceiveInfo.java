package com.uniConnect.collaboration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_receive_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentReceiveInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 협업(collaboration)에 속한 수령정보인지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    /** 수령인 이름 */
    private String receiverName;

    /** 수령 장소 */
    private String receivePlace;

    /** 비고 */
    private String note;
}