package com.telegram.debt;

import com.telegram.debt.model.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupDao  extends CrudRepository<Group, Long> {
}
