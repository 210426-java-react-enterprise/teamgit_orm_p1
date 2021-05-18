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
    @Column(name = "user_id", nullable = false, unique = true, type = "serial")
    private static int id;//can be primitive, or primitive wrapper; including java.util.Date; java.sql.Date;

    @Column(name = "username", nullable = false, unique = true, type = "varchar", length = "20")
    private String username;

    @Column(name = "password", nullable = false, unique = false, type = "varchar", length = "255")
    private String password;

    @Column(name = "email", nullable = false, unique = true, type = "varchar", length = "255")
    private String email;

    @Column(name = "first_name", nullable = false, unique = false, type = "varchar", length = "25")
    private String firstName;

    @Column(name = "last_name", nullable = false, unique = false, type = "varchar", length = "25")
    private String lastName;

    @Date
    @Column(name = "dob", nullable = false, unique = false, type = "date")
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
    @Constructor(name = "users")
    public AppUser(String username, String password, String email, String firstName, String lastName, String dob) {
        System.out.println("Registering user...");
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }
    //Getters
    @Getter(name = "user_id")
    public static int getId() {
        return id;
    }

    @Getter(name = "username")
    public String getUsername() {
        return username;
    }

    @Getter(name = "password")
    public String getPassword() {
        return password;
    }
    @Getter(name = "first_name")
    public String getFirstName() {
        return firstName;
    }
    @Getter(name = "last_name")
    public String getLastName() {
        return lastName;
    }
    @Getter(name = "email")
    public String getEmail() {
        return email;
    }

    @Getter(name = "dob")
    public String getDob() {
        return dob;
    }

    //Setters
    @Setter(name = "user_id")
    public int setId(int id) {
        this.id = id;
        return id;
    }

    @Setter(name = "username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Setter(name = "password")
    public void setPassword(String password) {
        this.password = password;
    }

    @Setter(name = "email")
    public void setEmail(String email) {
        this.email = email;
    }

    @Setter(name = "first_name")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Setter(name = "last_name")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Setter(name = "dob")
    public void setDob(String dob) {
        this.dob = dob;
    }
}
