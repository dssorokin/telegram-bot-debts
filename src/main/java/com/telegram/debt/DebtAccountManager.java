package com.telegram.debt;

import java.math.BigDecimal;
import java.util.Map;

public interface DebtAccountManager {

	void addDebtForGroup(BigDecimal debtSum, Long lenderId, Long borrowerId,
								Long groupId, String debtDescription) throws NoSuchUserException;

	Map<String, BigDecimal> calculateSummaryDebtsForUser(final String userName, final long groupId);

}
