package com.telegram.debt;

public interface UserManager {
	public void registerUserInGroup(Long user, Long groupId, String userName);

	public boolean isUserExisted(String userLink);
}
