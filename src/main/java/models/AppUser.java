package models;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.time.LocalDate;

//Thomas' note: models should be kept as separate entities from the ORM, and are not persistent data

/**
 * Builds the AppUser object using basic getters and setters
 */

@Entity(name = "appuser")
public class AppUser {

    @Id //Specifies Primary Key of entity
    @Column(name = "id")
    private static int id;//can be primitive, or primitive wrapper; including java.util.Date; java.sql.Date;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "dob")
    private String dob;//TODO: update format?


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
