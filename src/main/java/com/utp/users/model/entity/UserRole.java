package com.utp.users.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_roles")
public class UserRole {
  @Id
  private Long id;

  @Column("id_role")
  private Long idRole;

  @Column("id_user")
  private Long idUser;
}
