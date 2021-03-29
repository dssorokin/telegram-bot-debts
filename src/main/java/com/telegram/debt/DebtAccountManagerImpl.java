package com.telegram.debt;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author dsorokin on 24.03.2021
 */

@Component
public class DebtAccountManagerImpl implements DebtAccountManager {

	@Override
	public void addDebtForGroup(BigDecimal debtAmount, Long debtorId, Long groupId, String debtDescription) throws DebtAccountException {

	}
}
