package com.codeloom.backend.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @PersistenceCreator)
@With
@Table("topics")
public class Topic {
    @Id
    @Column("topic_id")
    private final UUID id;

    @Column("name")
    private final String name;
}
