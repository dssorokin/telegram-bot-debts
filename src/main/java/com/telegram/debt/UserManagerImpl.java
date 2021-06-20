package com.telegram.debt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.debt.model.Group;
import com.telegram.debt.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class UserManagerImpl implements UserManager {

    private final static Pattern LINK_PATTERN = Pattern.compile("@(\\w)");

    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private DebtAccountManager debtAccountManager;

    @Override
    public void registerUserInGroup(Long userId, Long groupId, String userName) {
        final Group group = groupDao.findById(groupId).orElseGet(() -> {
            Group newGroup = new Group();
            newGroup.setGroupId(groupId);
            return groupDao.save(newGroup);
        });

        User user = new User();
        user.setUserId(userId);
        user.setGroup(group);
        user.setName(userName);
        user.setShipmentDate(new Date());
        userDao.save(user);
    }

    @Override
    public User findUserByLink(String userLink) {
        Matcher userLinkMatcher = LINK_PATTERN.matcher(userLink);
        if (userLinkMatcher.matches()) {
            final String userName = userLinkMatcher.group(1);
            return userDao.findByName(userName);
        }
        return null;
    }
}
