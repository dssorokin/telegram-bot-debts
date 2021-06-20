package com.telegram.debt;

import com.telegram.debt.model.Debt;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.Map;

public interface DebtDao extends CrudRepository<Debt, Long> {

}
