package com.telegram.debt;

import com.telegram.debt.model.User;

public interface UserManager {
	void registerUserInGroup(Long user, Long groupId, String userName);

	User findUserByLink(String userLink);
}
