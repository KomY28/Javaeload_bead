package com.example.bank;

import java.time.LocalDateTime;

public class CurrentPrice {
    private String instrument;
    private double bid;
    private double ask;
    private LocalDateTime time;

    public CurrentPrice(String instrument, double bid, double ask) {
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.time = LocalDateTime.now();
    }

    // --- Csak Getterek kellenek ---
    public String getInstrument() { return instrument; }
    public double getBid() { return bid; }
    public double getAsk() { return ask; }
    public LocalDateTime getTime() { return time; }
}