package com.telegram.debt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.debt.exceptions.DebtException;
import com.telegram.debt.exceptions.NoSuchUserException;
import com.telegram.debt.model.Debt;
import com.telegram.debt.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void addDebtForGroup(BigDecimal debtAmount, Long lenderId, Long borrowerId,
								Long groupId, String debtDescription) throws NoSuchUserException, DebtException {
		User lender = userDao.findById(lenderId).orElseThrow(() -> new NoSuchUserException(lenderId));
		User borrower = userDao.findById(borrowerId).orElseThrow(() -> new NoSuchUserException(borrowerId));

		updateUsersDebtsSummary(lender, borrower, debtAmount);

		Debt newDebt = new Debt();
		newDebt.setAmount(debtAmount);
		newDebt.setFromUser(lenderId);
		newDebt.setToUser(borrowerId);
		newDebt.setCreatedDate(new Date());
		debtDao.save(newDebt);
	}

	private void updateUsersDebtsSummary(User lender, User borrower, BigDecimal debtAmount) throws DebtException {
		try {
			lender.setSummaryDebts(objectMapper.writeValueAsString(
			  calculateNewSummaryForUsers(lender, borrower, debtAmount, true)
            ));
			borrower.setSummaryDebts(objectMapper.writeValueAsString(
			  calculateNewSummaryForUsers(borrower, lender, debtAmount, false)
            ));

			userDao.saveAll(Arrays.asList(lender, borrower));
		} catch (JsonProcessingException e) {
			throw new DebtException(e);
		}
	}

	private Map<String, BigDecimal> calculateNewSummaryForUsers(User fromUser, User toUser, BigDecimal debtAmount, boolean fromLender) throws JsonProcessingException {
        TypeReference<HashMap<String, BigDecimal>> typeMap = new TypeReference<HashMap<String, BigDecimal>>() {};
        Map<String, BigDecimal> mapSummaryDebtsForFromUser = objectMapper.readValue(fromUser.getSummaryDebts(), typeMap);
        BigDecimal summaryAmounFromUserToUser = mapSummaryDebtsForFromUser.get(toUser.getName());
        if (summaryAmounFromUserToUser == null) {
            log.debug("Starts to count {}'s money in {}'s summary", toUser.getName(), fromUser.getName());
            mapSummaryDebtsForFromUser.put(fromUser.getName(), fromLender ? debtAmount : debtAmount.negate());
        } else {
            mapSummaryDebtsForFromUser.put(toUser.getName(), fromLender ? summaryAmounFromUserToUser.add(debtAmount) : summaryAmounFromUserToUser.subtract(debtAmount));
        }
        return mapSummaryDebtsForFromUser;
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
