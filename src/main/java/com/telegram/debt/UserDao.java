package com.telegram.debt;

import com.telegram.debt.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDao extends CrudRepository<User, Long> {

    User findByName(String userName);
}
