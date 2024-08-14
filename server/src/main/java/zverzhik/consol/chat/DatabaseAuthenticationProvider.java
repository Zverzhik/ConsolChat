package zverzhik.consol.chat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAuthenticationProvider  implements AuthenticationProvider {
    private final String USER;
    private final String PASSWORD;
    private final String DATABASE_URL = "jdbc:postgresql://localhost:5432/basic";

    public DatabaseAuthenticationProvider(String USER, String PASSWORD) {
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
    }

    private void addUsername(String user) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("INSERT INTO users username VALUES (?);")) {
                preparedStatement.setString(1, user);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void deleteUsername(String user) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM users WHERE username = (?)")) {
                preparedStatement.setString(1, user);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void deleteUsernameById(int id) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = (?)")) {
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            }
        }
    }

    private String getUsernameById(int id) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = (?)")) {
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
                return null;
            }
        }
    }

    private Integer getIdByUsername(String username) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = (?)")) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    private void updateUsernameById(int id, String username) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("UPDATE users SET username = (?) WHERE id = (?)")) {
                preparedStatement.setString(1, username);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        }
    }

    private boolean isUsernameExist(String username) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT username FROM users")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString("username").equals(username))
                        return true;
                }
                return false;
            }
        }
    }

    private void addRole(String role) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("INSERT INTO role name VALUES (?);")) {
                preparedStatement.setString(1, role);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void deleteRole(String role) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM role WHERE name = (?)")) {
                preparedStatement.setString(1, role);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void updateRoleById(int id, String name) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("UPDATE role SET name = (?) WHERE id = (?)")) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        }
    }

    private Integer getRoleId(String name) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT id FROM role WHERE name = (?)")) {
                preparedStatement.setString(1, name);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    private void addLoginAndPasswordToUser(int userId, String login, String password) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("INSERT INTO auth (user_id, login, password) VALUES (?, ?, ?);")) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(1, login);
                preparedStatement.setString(1, password);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void addLogAndPassToUser(String username, String login, String password) throws SQLException {
        addLoginAndPasswordToUser(getIdByUsername(username), login, password);
    }

    private void deleteLoginAndPasswordToUser(int userId) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM auth WHERE user_id = (?)")) {
                preparedStatement.setInt(1, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    private boolean isLoginExist(String login) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT login FROM auth")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString("login").equals(login))
                        return true;
                }
                return false;
            }
        }
    }

    private void addRoleToUser(String username, List<Role> role) throws SQLException {
        int userId = getIdByUsername(username);
        int roleId = getRoleId(role.toString());
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("INSERT INTO user_to_role (role_id, user_id) VALUES (?, ?);")) {
                preparedStatement.setInt(1, roleId);
                preparedStatement.setInt(1, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    private List<Role> getRoleByUsername(String username) throws SQLException {
        List<Role> roles = new ArrayList<>();
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT r.name AS name FROM user_to_role ur LEFT JOIN " +
                    "role r ON r.id = ur.role_id LEFT JOIN users u ON u.id = ur.user_id WHERE u.username = (?)")) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        roles.add(Role.valueOf(resultSet.getString("name")));
                    }
                    return roles;
                }
            }
        }
    }

    private boolean hasRoleByUsername(String username, Role role) throws SQLException {
        return getRoleByUsername(username).contains(role);
    }

    private void deleteAllRoleUser(String username) throws SQLException {
        int id = getIdByUsername(username);
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM user_to_role WHERE user_id = (?)")) {
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            }
        }
    }

    private void deleteRoleToUser(String username, Role role) throws SQLException {
        int userId = getIdByUsername(username);
        int roleId = getRoleId(role.name());
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM user_to_role WHERE user_id = (?) AND role_id = (?)")) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(1, roleId);
                preparedStatement.executeUpdate();
            }
        }
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username) throws SQLException {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().length() < 1) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }
        List<Role> role = new ArrayList<>();
        addUsername(username);
        addLogAndPassToUser(username, login, password);
        addRoleToUser(username, role);
        return true;
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) throws SQLException {
        try (var connection = getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT username FROM users u LEFT JOIN " +
                    "auth a ON u.id = a.user_id WHERE login(?) AND password = (?)")) {
                preparedStatement.setString(1, login);
                preparedStatement.setString(1, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("username");
                    }
                    return null;
                }
            }
        }
    }

    @Override
    public boolean isLoginAlreadyExist(String login) throws SQLException {
        return isLoginExist(login);
    }

    @Override
    public boolean isUsernameAlreadyExist(String username) throws SQLException {
        return isUsernameExist(username);
    }

    @Override
    public boolean isUserAdmin(String username) throws SQLException {
        return hasRoleByUsername(username, Role.ADMIN);
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) throws SQLException {
        return false;
    }
}
