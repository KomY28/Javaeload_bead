package com.example.bank;

public class ForexAccount {
    private long accountId;
    private String currency;
    private double balance;
    private double profitLoss;

    public ForexAccount(long accountId, String currency, double balance, double profitLoss) {
        this.accountId = accountId;
        this.currency = currency;
        this.balance = balance;
        this.profitLoss = profitLoss;
    }

    // --- Getterek ---
    public long getAccountId() { return accountId; }
    public String getCurrency() { return currency; }
    public double getBalance() { return balance; }
    public double getProfitLoss() { return profitLoss; }

    // --- ÚJ SETTEREK ---
    // Ezek kellenek, hogy a ForexService módosítani tudja az értékeket

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }
}