-- shipping_info 테이블 생성
CREATE TABLE shipping_info (
    id BIGSERIAL PRIMARY KEY,
    collaboration_id BIGINT NOT NULL,
    shipping_date DATE,
    tracking_number VARCHAR(255),
    is_shipped BOOLEAN DEFAULT FALSE,
    note TEXT,
    CONSTRAINT fk_shipping_collaboration
        FOREIGN KEY (collaboration_id)
        REFERENCES collaborations (collaboration_id)
        ON DELETE CASCADE
);

-- student_receive_info 테이블 생성
CREATE TABLE student_receive_info (
    id BIGSERIAL PRIMARY KEY,
    collaboration_id BIGINT NOT NULL,
    receiver_name VARCHAR(100),
    receive_place VARCHAR(255),
    note TEXT,
    CONSTRAINT fk_receive_collaboration
        FOREIGN KEY (collaboration_id)
        REFERENCES collaborations (collaboration_id)
        ON DELETE CASCADE
);