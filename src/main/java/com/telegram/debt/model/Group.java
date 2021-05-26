package com.telegram.debt.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "group")
@Table(name = "telegram_group")
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @OneToMany(mappedBy = "group")
    private Set<User> users;
}
