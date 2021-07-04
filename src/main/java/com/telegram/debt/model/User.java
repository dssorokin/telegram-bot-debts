package com.telegram.debt.model;

import com.telegram.debt.postgres.JsonDataUserType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Entity(name = "users")
@Table(name = "users")
@TypeDef(name = "JsonDataUserType", typeClass = JsonDataUserType.class)
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

    @Type(type = "JsonDataUserType")
    private Map<String, BigDecimal> summaryDebts;

}
