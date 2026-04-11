package com.pos.service;

import com.pos.dao.UserDAO;
import com.pos.model.User;

import java.util.Arrays;
import java.util.List;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private static final List<String> ALLOWED_USERS = Arrays.asList("admin", "owner");
    private static User loggedInUser = null;

    public void initDefaultUsers() {
        userDAO.insertIfNotExists("admin", "123");
        userDAO.insertIfNotExists("owner", "123");
    }

    public User login(String username, String password) {
        if (!ALLOWED_USERS.contains(username.toLowerCase())) {
            return null;
        }
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return user;
        }
        return null;
    }

    public static User getLoggedInUser() { return loggedInUser; }

    public static void logout() { loggedInUser = null; }
}
