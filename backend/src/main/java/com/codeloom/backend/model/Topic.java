package com.codeloom.backend.model;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

@Table("topics")
public class Topic {
  @Id
  @Column("topic_id")
  private UUID id;

  @Column("name")
  private String name;

  public Topic(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public Topic(String name) {
    this(null, name);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
