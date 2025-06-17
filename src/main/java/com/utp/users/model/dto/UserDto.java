package com.utp.users.model.dto;

import com.utp.users.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private Long idUser;
  private String username;
  private String name;
  private String lastname;
  private String dni;
  private String institutionalEmail;
  private String career;
  private Boolean actualRegistered;
  private List<Role> roles;
  private CampusDto campus;
}
