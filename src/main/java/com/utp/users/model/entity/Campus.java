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
@Table("campus")
public class Campus {
    @Id
    @Column("id_campus")
    private Long idCampus;

    @Column("name_campus")
    private String nameCampus;
}
