package com.utp.portal.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("cycles")
public class Cycle {
  @Id
  @Column("id_cycle")
  private Long idCycle;

  @Column("name_cycle")
  private String nameCycle;

  @Column("start_date")
  private LocalDate startDate;

  @Column("end_date")
  private LocalDate endDate;
}
