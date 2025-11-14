package com.example.bank;

public class ForexPosition {

    private long tradeId;
    private String instrument;
    private int quantity;
    private double openPrice;

    // Üres konstruktor (kellhet a későbbiekben)
    public ForexPosition() {
    }

    // Konstruktor az adatok gyors létrehozásához
    public ForexPosition(long tradeId, String instrument, int quantity, double openPrice) {
        this.tradeId = tradeId;
        this.instrument = instrument;
        this.quantity = quantity;
        this.openPrice = openPrice;
    }

    // Getterek (ezeket fogja olvasni a Thymeleaf)
    public long getTradeId() {
        return tradeId;
    }

    public String getInstrument() {
        return instrument;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    // (Opcionális: Setterek, ha később módosítani is akarjuk)
}