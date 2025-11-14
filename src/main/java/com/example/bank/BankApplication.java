package com.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage;
import soapclient.MNBArfolyamServiceSoapImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;


@SpringBootApplication
@Controller
public class BankApplication {

    @Autowired
    private ForexService forexService;

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    // --- MNB SOAP RÉSZ (VÁLTOZATLAN) ---
    @GetMapping("/exercise")
    public String soap1(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "form";
    }

    @PostMapping("/exercise")
    public String soap2(@ModelAttribute MessagePrice messagePrice, Model model)
            throws MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage,
            ParserConfigurationException, SAXException, IOException {

        MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
        MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();
        String mnbResponseXml = service.getExchangeRates(
                messagePrice.getStartDate(), messagePrice.getEndDate(), messagePrice.getCurrency());

        List<String> priceLabels = new ArrayList<>();
        List<Double> priceData = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(mnbResponseXml));
        Document doc = builder.parse(is);
        doc.getDocumentElement().normalize();
        NodeList dayList = doc.getElementsByTagName("Day");

        for (int i = 0; i < dayList.getLength(); i++) {
            Node dayNode = dayList.item(i);
            if (dayNode.getNodeType() == Node.ELEMENT_NODE) {
                Element dayElement = (Element) dayNode;
                String date = dayElement.getAttribute("date");
                Element rateElement = (Element) dayElement.getElementsByTagName("Rate").item(0);
                String rateString = rateElement.getTextContent();
                String normalizedRateString = rateString.replace(",", ".");
                double rateValue = Double.parseDouble(normalizedRateString);
                priceLabels.add(date);
                priceData.add(rateValue);
            }
        }
        Collections.reverse(priceLabels);
        Collections.reverse(priceData);
        model.addAttribute("priceLabels", priceLabels);
        model.addAttribute("priceData", priceData);
        return "result";
    }

    // --- ÚJ FOREX RÉSZEK ---

    /**
     * Forex-account: Számlainformációk kiíratása.
     */
    @GetMapping("/forex-account")
    public String showAccount(Model model) {
        model.addAttribute("account", forexService.getAccountInfo());
        return "forex-account"; // Új HTML kell
    }

    /**
     * Forex-Poz: Nyitott pozíciók (Ezt már megírtuk)
     */
    @GetMapping("/forex-positions")
    public String showPositions(Model model) {
        model.addAttribute("positions", forexService.getOpenPositions());
        return "forex-positions";
    }

    /**
     * Forex-AktÁr (GET): Lenyíló lista mutatása.
     */
    @GetMapping("/forex-price")
    public String showCurrentPriceForm(Model model) {
        // Átadjuk az űrlap-modellt és a választható instrumentumokat
        model.addAttribute("formRequest", new FormRequest());
        model.addAttribute("instruments", forexService.getAvailableInstruments());
        // (Az eredmény (currentPrice) itt még null)
        return "forex-price"; // Új HTML kell
    }

    /**
     * Forex-AktÁr (POST): Űrlap feldolgozása és ár kiíratása.
     */
    @PostMapping("/forex-price")
    public String getCurrentPrice(@ModelAttribute FormRequest formRequest, Model model) {
        // Lekérjük az árat a service-től
        CurrentPrice price = forexService.getCurrentPrice(formRequest.getInstrument());

        // Visszaadjuk ugyanazt, mint a GET, de kiegészítve az eredménnyel
        model.addAttribute("currentPrice", price); // Az eredmény
        model.addAttribute("formRequest", formRequest); // Az űrlap adatai (hogy a select ne "resetelődjön")
        model.addAttribute("instruments", forexService.getAvailableInstruments()); // A lista
        return "forex-price"; // Maradunk ugyanazon az oldalon
    }

    /**
     * Forex-HistÁr (GET): Történelmi adatok űrlapjának mutatása.
     */
    @GetMapping("/forex-history")
    public String showHistoryForm(Model model) {
        model.addAttribute("formRequest", new FormRequest());
        model.addAttribute("instruments", forexService.getAvailableInstruments());
        model.addAttribute("granularities", forexService.getAvailableGranularities());
        return "forex-history"; // Új HTML kell
    }

    /**
     * Forex-HistÁr (POST): Űrlap feldolgozása és árak kiíratása.
     */
    @PostMapping("/forex-history")
    public String getHistory(@ModelAttribute FormRequest formRequest, Model model) {
        // Lekérjük a 10 történelmi adatot
        List<HistoricalPrice> prices = forexService.getHistoricalPrices(
                formRequest.getInstrument(),
                formRequest.getGranularity()
        );

        // Visszaadjuk az űrlapot és az eredményt
        model.addAttribute("historicalPrices", prices);
        model.addAttribute("formRequest", formRequest);
        model.addAttribute("instruments", forexService.getAvailableInstruments());
        model.addAttribute("granularities", forexService.getAvailableGranularities());
        return "forex-history"; // Maradunk ugyanazon az oldalon
    }

    /**
     * Forex-Nyit (GET): Pozíció nyitása űrlap.
     */
    @GetMapping("/forex-open")
    public String showOpenTradeForm(Model model) {
        model.addAttribute("formRequest", new FormRequest());
        model.addAttribute("instruments", forexService.getAvailableInstruments());
        return "forex-open"; // Új HTML kell
    }

    /**
     * Forex-Nyit (POST): Pozíció nyitása és átirányítás.
     */
    @PostMapping("/forex-open")
    public String openTrade(@ModelAttribute FormRequest formRequest) {
        forexService.openTrade(
                formRequest.getInstrument(),
                formRequest.getQuantity()
        );
        // FONTOS: Átirányítjuk a pozíciós listára (Post-Redirect-Get pattern)
        return "redirect:/forex-positions";
    }

    /**
     * Forex-Zár (GET): Pozíció zárása űrlap.
     */
    @GetMapping("/forex-close")
    public String showCloseTradeForm(Model model) {
        model.addAttribute("formRequest", new FormRequest());
        // Bónusz: Átadjuk a nyitott ID-kat a dropdown-hoz
        model.addAttribute("openTradeIds",
                forexService.getOpenPositions().stream()
                        .map(ForexPosition::getTradeId)
                        .collect(Collectors.toList())
        );
        return "forex-close"; // Új HTML kell
    }

    /**
     * Forex-Zár (POST): Pozíció zárása és átirányítás.
     */
    @PostMapping("/forex-close")
    public String closeTrade(@ModelAttribute FormRequest formRequest) {
        forexService.closeTrade(formRequest.getTradeId());
        // Átirányítás a pozíciós listára
        return "redirect:/forex-positions";
    }
}