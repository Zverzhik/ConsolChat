package zverzhik.consol.chat;

import java.sql.SQLException;

public interface AuthenticationProvider {
    void initialize();
    boolean authenticate(ClientHandler clientHandler, String login, String password) throws SQLException;
    boolean registration(ClientHandler clientHandler, String login, String password, String username)throws SQLException;
    String getUsernameByLoginAndPassword(String login, String password) throws SQLException;
    boolean isLoginAlreadyExist(String login) throws SQLException;
    boolean isUsernameAlreadyExist(String username) throws SQLException;
    boolean isUserAdmin(String username) throws SQLException;
}
