package com.pos.service;

import com.pos.dao.UserDAO;
import com.pos.model.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private static User loggedInUser = null;

    public void initDefaultUsers() {
        userDAO.insertIfNotExists("admin", "admin123");
        userDAO.insertIfNotExists("owner", "owner123");
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty() || password.isBlank()) {
            return null;
        }

        User user = userDAO.findByUsername(normalizedUsername);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return user;
        }
        return null;
    }

    public static User getLoggedInUser() { return loggedInUser; }

    public static void logout() { loggedInUser = null; }
}
