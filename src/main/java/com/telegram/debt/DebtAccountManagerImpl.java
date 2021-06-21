package com.telegram.debt;

import com.telegram.debt.model.Debt;
import com.telegram.debt.model.User;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dsorokin on 24.03.2021
 */

@Component
@Slf4j
public class DebtAccountManagerImpl implements DebtAccountManager {

	@Autowired
	private UserDao userDao;

	@Autowired
	private DebtDao debtDao;

	@Override
	public void addDebtForGroup(BigDecimal debtAmount, Long lenderId, Long borrowerId,
								Long groupId, String debtDescription) throws NoSuchUserException {
		User lender = userDao.findById(lenderId).orElseThrow(() -> new NoSuchUserException(lenderId));
		User borrower = userDao.findById(borrowerId).orElseThrow(() -> new NoSuchUserException(borrowerId));



		Debt newDebt = new Debt();
		newDebt.setAmount(debtAmount);
		newDebt.setFromUser(lenderId);
		newDebt.setToUser(borrowerId);
		debtDao.save(newDebt);
	}

	private void updateUsersDebtsSummary(User lender, User borrowerId, BigDecimal debtAmount) {

	}

	@Override
	public Map<String, BigDecimal> calculateSummaryDebtsForUser(final String userName,final long groupId) {
		Map<String, BigDecimal> summaryDebts = userDao.calculateDebtsSummaryForUser(userName, groupId);
		summaryDebts.entrySet().forEach(onePersonPerDebt -> {
			BigDecimal userDebt = onePersonPerDebt.getValue();
			BigDecimal invertDebt = userDebt.compareTo(BigDecimal.ZERO) > 0 ? userDebt.negate() : userDebt.abs();
			onePersonPerDebt.setValue(invertDebt);
		});
		return summaryDebts;
	}
}
