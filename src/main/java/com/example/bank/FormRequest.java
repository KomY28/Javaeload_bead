package com.example.bank;

// Ezt az osztályt fogják a Thymeleaf űrlapok kitölteni
public class FormRequest {

    // Forex-AktÁr, Forex-HistÁr, Forex-Nyit
    private String instrument;

    // Forex-HistÁr
    private String granularity;

    // Forex-Nyit
    private int quantity;

    // Forex-Zár
    private Long tradeId; // <-- JAVÍTÁS: long helyett Long

    // --- Getterek ÉS Setterek (Ezek KÖTELEZŐEK az űrlapokhoz) ---

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public String getGranularity() { return granularity; }
    public void setGranularity(String granularity) { this.granularity = granularity; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // JAVÍTVA
    public Long getTradeId() { return tradeId; }
    public void setTradeId(Long tradeId) { this.tradeId = tradeId; }
}