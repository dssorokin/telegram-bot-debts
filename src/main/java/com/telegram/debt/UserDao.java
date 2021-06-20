package com.telegram.debt;

import com.telegram.debt.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserDao extends CrudRepository<User, Long> {

    User findByName(String userName);

    List<User> findByGroupAndUserIdNot(Long groupId, Long userId);

    @Query(value = "select name, summary_debts -> ?1 from users where group_id = ?2;", nativeQuery = true)
    public Map<String, BigDecimal> calculateDebtsSummaryForUser(String userName, long groupId);
}
