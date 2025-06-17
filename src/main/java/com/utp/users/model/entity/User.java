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
@Table("users")
public class User {
  @Id
  @Column("id_user")
  private Long idUser;

  @Column("id_campus")
  private Long idCampus;
  private String username;
  private String password;
  private String name;

  @Column("last_name")
  private String lastname;

  private String dni;

  @Column("institutional_email")
  private String institutionalEmail;

  @Column("career")
  private String career;

  @Column("actual_registered")
  private Boolean actualRegistered;
}
