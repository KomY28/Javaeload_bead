package com.example.bank;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ForexService {

    // --- ÁL ADATBÁZISOK ---

    // A számla adatai (már nem 'final', hogy cserélhető legyen, bár csak a tartalmát módosítjuk)
    private final ForexAccount accountInfo = new ForexAccount(123456, "USD", 10000.0, 0.0);

    // A nyitott pozíciók listája
    private final List<ForexPosition> openPositions = new ArrayList<>(Arrays.asList(
            new ForexPosition(1001, "EUR_USD", 10000, 1.0850),
            new ForexPosition(1002, "USD_JPY", -5000, 145.20),
            new ForexPosition(1003, "GBP_USD", 2000, 1.2510)
    ));

    // Aktuális (kitalált) árak
    private final Map<String, Double> currentPrices = Map.of(
            "EUR_USD", 1.0830,
            "USD_JPY", 146.10,
            "GBP_USD", 1.2550,
            "AUD_USD", 0.6500
    );

    // Választható instrumentumok
    public List<String> getAvailableInstruments() {
        return new ArrayList<>(currentPrices.keySet());
    }

    // Választható granularitások
    public List<String> getAvailableGranularities() {
        return Arrays.asList("M1", "M15", "H1", "H4", "D");
    }

    // --- Forex-account ---
    public ForexAccount getAccountInfo() {
        return this.accountInfo;
    }

    // --- Forex-Poz ---
    public List<ForexPosition> getOpenPositions() {
        return this.openPositions;
    }

    // --- Forex-AktÁr ---
    public CurrentPrice getCurrentPrice(String instrument) {
        if (!currentPrices.containsKey(instrument)) {
            return null;
        }
        double price = currentPrices.get(instrument);
        double bid = price - (price * 0.0001);
        double ask = price + (price * 0.0001);
        return new CurrentPrice(instrument, bid, ask);
    }

    // --- Forex-HistÁr ---
    public List<HistoricalPrice> getHistoricalPrices(String instrument, String granularity) {
        List<HistoricalPrice> prices = new ArrayList<>();
        double startPrice = currentPrices.getOrDefault(instrument, 1.0);
        for (int i = 10; i > 0; i--) {
            double open = startPrice;
            double close = open + ThreadLocalRandom.current().nextDouble(-0.01, 0.01);
            double high = Math.max(open, close) + ThreadLocalRandom.current().nextDouble(0, 0.005);
            double low = Math.min(open, close) - ThreadLocalRandom.current().nextDouble(0, 0.005);
            prices.add(new HistoricalPrice("2025-10-" + (20 - i), open, high, low, close));
            startPrice = close;
        }
        return prices;
    }

    // --- Forex-Nyit (MÓDOSÍTVA) ---
    public void openTrade(String instrument, int quantity) {
        if (quantity == 0) return; // Nincs mit nyitni

        double openPrice = currentPrices.getOrDefault(instrument, 1.0);

        // --- ÚJ LOGIKA: Egyenleg módosítása ---
        // Ellenőrizzük, hogy az account pénzneme (USD) egyezik-e a pár "quote" pénznemével
        String accountCurrency = this.accountInfo.getCurrency(); // "USD"

        // Csak akkor módosítjuk, ha a pár pl. "EUR_USD" (a quote currency USD)
        if (instrument.endsWith("_" + accountCurrency)) {

            // Költség = mennyiség * ár
            // Ha veszünk (quantity > 0), a balance CSÖKKEN.
            // Ha eladunk (quantity < 0), a balance NŐ.
            // Ezért a balance változása: -(mennyiség * ár)
            double cost = quantity * openPrice;
            double currentBalance = this.accountInfo.getBalance();

            // Frissítjük a központi accountInfo objektum egyenlegét
            this.accountInfo.setBalance(currentBalance - cost);
        }
        // Ha a pár pl. "USD_JPY", nem csinálunk semmit a balance-szal (mert JPY-ben lenne a költség)
        // --- ÚJ LOGIKA VÉGE ---

        long newId = ThreadLocalRandom.current().nextLong(1004, 9999);
        openPositions.add(new ForexPosition(newId, instrument, quantity, openPrice));
    }

    // --- Forex-Zár (MÓDOSÍTVA) ---
    public void closeTrade(Long tradeId) {
        if (tradeId == null || tradeId == 0) {
            return;
        }

        // 1. Keressük meg a pozíciót (de még ne töröljük)
        ForexPosition positionToClose = null;
        for (ForexPosition pos : openPositions) {
            if (pos.getTradeId() == tradeId) {
                positionToClose = pos;
                break;
            }
        }

        // Ha megvan a pozíció
        if (positionToClose != null) {

            // --- ÚJ LOGIKA: Egyenleg frissítése ---
            String instrument = positionToClose.getInstrument();
            String accountCurrency = this.accountInfo.getCurrency();

            // Csak akkor módosítjuk, ha a pár pl. "EUR_USD"
            if (instrument.endsWith("_" + accountCurrency)) {

                // 2. Vegyük az aktuális (záró) árat
                double closePrice = currentPrices.getOrDefault(instrument, positionToClose.getOpenPrice());

                // 3. Számoljuk ki a záró értéket
                // Amikor nyitottuk, levontuk: (quantity * openPrice)
                // Amikor zárunk, visszatesszük: (quantity * closePrice)
                double closingValue = positionToClose.getQuantity() * closePrice;

                // 4. Frissítjük az egyenleget
                double currentBalance = this.accountInfo.getBalance();
                this.accountInfo.setBalance(currentBalance + closingValue);

                // 5. (Bónusz) Frissítsük a realizált P/L-t is
                double profit = (closePrice - positionToClose.getOpenPrice()) * positionToClose.getQuantity();
                this.accountInfo.setProfitLoss(this.accountInfo.getProfitLoss() + profit);
            }
            // --- ÚJ LOGIKA VÉGE ---

            // 6. Töröljük a pozíciót a listából
            openPositions.remove(positionToClose);
        }
    }
}