package models;


import annotations.*;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.time.LocalDate;

//Thomas' note: models should be kept as separate entities from the ORM, and are not persistent data

/**
 * Builds the AppUser object using basic getters and setters
 */

@Entity()
@Table(name = "users")
public class AppUser {

    @Id(name = "user_id") //Specifies Primary Key of entity
    @Column(name = "user_id", nullable = false, unique = true)
    private static int id;//can be primitive, or primitive wrapper; including java.util.Date; java.sql.Date;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, unique = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false, unique = false)
    private String firstName;

    @Column(name = "last_name", nullable = false, unique = false)
    private String lastName;

    @Column(name = "dob", nullable = false, unique = false)
    @Date
    private String dob;


    public AppUser() {
        super();
    }

    /**
     * Assembles the present values of the params to assemble an AppUser object to be more easily passed around
     * Values for the params will be harvested at registration.
     * @param username String
     * @param password String
     * @param email String
     * @param firstName String
     * @param lastName String
     * @param dob String
     */

    //dob should be YYYY-MM-DD format, regex
    public AppUser(String username, String password, String email, String firstName, String lastName, String dob) {
        System.out.println("Registering user...");
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }

    public static int getId() {
        return id;
    }

    public int setId(int id) {
        this.id = id;
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() {
        return dob;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDateOfBirth() {
        return dob;
    }

    public void setDateOfBirth(String dob) {
        this.dob = dob;
    }
}