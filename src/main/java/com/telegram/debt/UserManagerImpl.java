package com.telegram.debt;

import com.telegram.debt.model.Group;
import com.telegram.debt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserManagerImpl implements UserManager {

    private final static Pattern LINK_PATTERN = Pattern.compile("@(\\w)");

    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupDao groupDao;

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
    public boolean isUserExisted(String userLink) {
        Matcher userLinkMatcher = LINK_PATTERN.matcher(userLink);
        if (userLinkMatcher.matches()) {
            final String userName = userLinkMatcher.group(1);
            User user = userDao.findByName(userName);
            return user != null;
        }
        return false;
    }
}
