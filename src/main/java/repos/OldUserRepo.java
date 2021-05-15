package repos;

import models.AppUser;
import util.ConnectionFactory;

import java.sql.*;
import java.sql.Date;

/**
 * Handles calls to the users table for user creation, and look up.
 */
public class OldUserRepo {

    /**
     * Inserts the new users records into the users table upon registration
     * @param newUser AppUser
     */
    public void save(AppUser newUser) {

        String username = newUser.getUsername();
        String password = newUser.getPassword();
        String firstName = newUser.getFirstName();
        String lastName = newUser.getLastName();
        String email = newUser.getEmail();
        String dob = newUser.getDob();

        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String user_insert = "INSERT INTO public.users (username, password, first_name, last_name, email, dob)" + "values (?,?,?,?,?,?)";

            PreparedStatement pstmt = conn.prepareStatement(user_insert, new String[] { "id" });
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setDate(6, Date.valueOf(dob));

            int rowsInserted = pstmt.executeUpdate();;
            if (rowsInserted != 0){
                ResultSet rs = pstmt.getGeneratedKeys();
                while(rs.next()){
                    int id = newUser.setId(rs.getInt("id"));
                    String account_insert = "INSERT INTO public.accounts (id)" + "values (?)";
                    PreparedStatement prepState = conn.prepareStatement(account_insert, new String[] { "account_num" });
                    prepState.setInt(1, id);
                    rowsInserted = prepState.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a given username is already in use in the users table, be used after an attempted registration
     * @param username String
     * @return boolean, true if the username is unique and allows registration to proceed or false,
     * upon which the user will be told to try another username
     */
    public boolean isUsernameAvailable(String username) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {

            String sql = "select * from quizzard.users where username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;

    }
    /**
     * Checks if a given email is already in use in the users table, be used after an attempted registration
     * @param email String
     * @return boolean, true if the email is unique and allows registration to proceed or false,
     * upon which the user will be told they are already registered
     */
    public boolean isEmailAvailable(String email) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {

            String sql = "select * from public.users where email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Queries the DB to see if a given username, password pair are associated in the same entry in the users table
     * if so it will log that information in the the AppUser object 'user'
     * @param username String
     * @param password String
     * @return the AppUser object 'user' to be used throughout the rest of the program
     */
    public AppUser findUserByUsernameAndPassword(String username, String password) {

        AppUser user = null;

        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {

            String sql = "SELECT * FROM public.users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                user = new AppUser();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setDateOfBirth(rs.getString("dob"));//TODO: update format?
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;

    }

}
