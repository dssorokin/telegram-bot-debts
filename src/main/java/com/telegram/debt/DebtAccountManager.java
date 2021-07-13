package com.telegram.debt;

import com.telegram.debt.exceptions.DebtException;
import com.telegram.debt.exceptions.NoSuchUserException;

import java.math.BigDecimal;
import java.util.Map;

public interface DebtAccountManager {

	 void addDebtForGroup(BigDecimal debtSum, Long lenderId, Long borrowerId,
								Long groupId, String debtDescription) throws NoSuchUserException, DebtException;

	Map<String, BigDecimal> getDebtsSummaryForUser(long userId) throws DebtException;

	void payForDebt(final Long debtorId, final String lenderName ,final BigDecimal paidDebtAmount, final long groupId) throws NoSuchUserException, DebtException;

}
