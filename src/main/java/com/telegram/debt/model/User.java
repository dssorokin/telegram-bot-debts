package com.telegram.debt.model;

import lombok.Data;
import org.springframework.stereotype.Controller;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "users")
@Table(name = "users")
@Data
public class User {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "shipment_date")
    private Date shipmentDate;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "summary_debts")
    private String summaryDebts;

}
