package com.codeloom.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@With
@Table("topics")
public class Topic {
    @Id
    @Column("topic_id")
    private final UUID id;

    @Column("name")
    private final String name;
}
