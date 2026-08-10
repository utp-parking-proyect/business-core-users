package com.utp.portal.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("role")
public class Role {
  @Id
  @Column("id_role")
  private Long idRole;

  @Column("name_role")
  private String name;
}
