package com.telegram.debt.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "debt")
@Table(name = "debts")
@Data
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "debt_id")
    private Long id;

    @Column(name = "from_user_id")
    private Long fromUser;

    @Column(name = "to_user_id")
    private Long toUser;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "amount")
    private BigDecimal amount;
}
