package models;

import annotations.*;

/**
 * holds the account number, balance and user id
 * @author Chris
 */
@Entity()
@Table(name = "accounts")
public class UserAccount {

    @Id(name = "account_num")
    @Column(name = "account_num", nullable = false, unique = true, type = "serial", updateable = false)
    private static int account_num;

    //Foreign key states the column name of the id, as well as the table it references
    @ForeignKey(name = "user_id", references = "users")
    @Column(name = "user_id", nullable = false, unique = true, type = "int", updateable = false)
    private static int id;

    @Column(name = "balance", nullable = false, unique = false, type = "double", length = "12,2", updateable = true)
    private double balance;

    public static int getAccount_num() {
        return account_num;
    }

    public static void setAccount_num(int account_num) {
        UserAccount.account_num = account_num;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        UserAccount.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public UserAccount(int id, double balance) {
        System.out.println("Registering user...");
        this.id = id;
        this.balance = balance;
    }
}
