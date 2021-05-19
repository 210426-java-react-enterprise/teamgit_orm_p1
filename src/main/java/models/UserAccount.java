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
    @Column(name = "account_num", nullable = false, unique = true)
    private static int account_num;

    @Column(name = "user_id", nullable = false, unique = true)
    private static int id;

    @Column(name = "balance", nullable = false, unique = false)
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
