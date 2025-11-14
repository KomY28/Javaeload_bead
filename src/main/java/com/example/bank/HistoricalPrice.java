package com.example.bank;

public class HistoricalPrice {
    private String date;
    private double open;
    private double high;
    private double low;
    private double close;

    public HistoricalPrice(String date, double open, double high, double low, double close) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    // --- Csak Getterek kellenek ---
    public String getDate() { return date; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
}