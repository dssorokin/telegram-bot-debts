package com.telegram.debt;

import java.math.BigDecimal;

public interface DebtAccountManager {

	public void addDebtForGroup(BigDecimal debtSum, Long debtorId, Long groupId, String debtDescription) throws DebtAccountException;

}
