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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dsorokin on 24.03.2021
 */

@Component
@Slf4j
public class DebtAccountManagerImpl implements DebtAccountManager {

	public static final TypeReference<HashMap<String, BigDecimal>> TYPE_MAP = new TypeReference<HashMap<String, BigDecimal>>() {
	};
	private final static Pattern LINK_PATTERN = Pattern.compile("@(\\w+)");

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
		newDebt.setDescription(debtDescription);
		debtDao.save(newDebt);
	}

	private void updateUsersDebtsSummary(User lender, User borrower, BigDecimal debtAmount) throws DebtException {
		try {
			lender.setSummaryDebts(calculateNewSummaryForUsers(lender, borrower, debtAmount, true));
			borrower.setSummaryDebts(calculateNewSummaryForUsers(borrower, lender, debtAmount, false));

			userDao.saveAll(Arrays.asList(lender, borrower));
		} catch (JsonProcessingException e) {
			throw new DebtException(e);
		}
	}

	private Map<String, BigDecimal> calculateNewSummaryForUsers(User fromUser, User toUser, BigDecimal debtAmount, boolean fromLender) throws JsonProcessingException {
        Map<String, BigDecimal> mapSummaryDebtsForFromUser = fromUser.getSummaryDebts();
        BigDecimal summaryAmounFromUserToUser = mapSummaryDebtsForFromUser.get(toUser.getName());
        if (summaryAmounFromUserToUser == null) {
            log.debug("Starts to count {}'s money in {}'s summary", toUser.getName(), fromUser.getName());
            mapSummaryDebtsForFromUser.put(toUser.getName(), fromLender ? debtAmount : debtAmount.negate());
        } else {
            mapSummaryDebtsForFromUser.put(toUser.getName(), fromLender ? summaryAmounFromUserToUser.add(debtAmount) : summaryAmounFromUserToUser.subtract(debtAmount));
        }
        return mapSummaryDebtsForFromUser;
	}

	@Override
	public Map<String, BigDecimal> getDebtsSummaryForUser(long userId) throws DebtException {
		User user = userDao.findById(userId).orElseThrow(() -> new DebtException());
		return user.getSummaryDebts();
	}

	@Override
	public void payForDebt(Long debtorId, String lenderLink, BigDecimal paidDebtAmount, final long groupId) throws NoSuchUserException, DebtException {
		User lenderUser = findUserByLink(lenderLink).orElseThrow(() -> new NoSuchUserException(debtorId));

		addDebtForGroup(paidDebtAmount,debtorId , lenderUser.getUserId(), groupId, "Отдача долга");
	}


	private Optional<User> findUserByLink(String userLink) {
		Matcher userLinkMatcher = LINK_PATTERN.matcher(userLink);
		if (userLinkMatcher.matches()) {
			final String userName = userLinkMatcher.group(1);
			return userDao.findByName(userName);
		}
		return Optional.empty();
	}
}
