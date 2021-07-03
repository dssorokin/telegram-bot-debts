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

	public static final TypeReference<HashMap<String, BigDecimal>> TYPE_MAP = new TypeReference<HashMap<String, BigDecimal>>() {
	};
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
        Map<String, BigDecimal> mapSummaryDebtsForFromUser = objectMapper.readValue(fromUser.getSummaryDebts(), TYPE_MAP);
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
	public Map<String, BigDecimal> getDebtsSummaryForUser(long userId) throws DebtException {
		User user = userDao.findById(userId).orElseThrow(() -> new DebtException());

		try {
			return objectMapper.readValue(user.getSummaryDebts(), TYPE_MAP);
		} catch (JsonProcessingException e) {
			throw new DebtException();
		}
	}
}
