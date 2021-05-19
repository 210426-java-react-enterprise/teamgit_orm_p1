package models;

/**
 * holds the deposit and withdrawal values for transactions
 * @author Chris
 */
public class TransactionValues {
    private static double deposit;

    private static double withdrawal;

    public static double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public static double getWithdrawal() {
        return withdrawal;
    }

    public void setWithdrawal(double withdrawal) {
        this.withdrawal = withdrawal;
    }

    public TransactionValues(double deposit, double withdrawal) {
        this.deposit = deposit;
        this.withdrawal = withdrawal;
    }
}
