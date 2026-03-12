package com.sprint.findex.domain.integration.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "index_info")
@Getter
public class IndexInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 지수 코드, 지수 이름 등 지수 자체의 정보들이 들어갑니다.
  private String indexCode;
  private String indexName;
}
