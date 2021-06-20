package com.telegram.debt;

import com.telegram.debt.model.User;

public interface UserManager {
	public void registerUserInGroup(Long user, Long groupId, String userName);

	public User findUserByLink(String userLink);
}
