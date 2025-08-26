package com.getgoods.hotelservice.scraper;

import com.getgoods.hotelservice.model.Hotels;
import com.getgoods.hotelservice.util.HotelsNotFoundException;
import com.getgoods.hotelservice.util.ScrapingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HotelScrapper {

    private static final String BASE_URL = "https://hotels.ng";
    private static final int TIMEOUT = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int MAX_HOTELS = 18;

    public List<Hotels> findHotels(String city, String country, Integer date) {
        List<Hotels> hotels = new ArrayList<>();
        Set<String> hotelNames = new HashSet<>();

        try {
            String cityUrl = buildHotelsNgUrl(city);
            Document doc = Jsoup.connect(cityUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
            Elements hotelElements = extractHotelElements(doc);

            for (Element hotelElement : hotelElements) {
                if (hotels.size() >= MAX_HOTELS) break;

                try {
                    Hotels hotel = extractHotelFromElement(hotelElement, city, country, date);
                    if (hotel != null && hotel.getHotelName() != null &&
                            hotelNames.add(hotel.getHotelName().toLowerCase().trim())) {
                        hotels.add(hotel);
                    }
                } catch (Exception e) {
                    throw new HotelsNotFoundException("Hotel not found try Nigerian hotels");
                }
            }
        } catch (Exception e) {
            throw new ScrapingException("Scraping failed");
        }

        return hotels.subList(0, Math.min(hotels.size(), MAX_HOTELS));
    }

    private String buildHotelsNgUrl(String city) {
        String citySlug = city.toLowerCase().replace(" ", "-").replace("'", "").trim();

        switch (citySlug) {
            case "port-harcourt": case "ph": return BASE_URL + "/hotels-in-rivers";
            case "abuja": case "fct": return BASE_URL + "/hotels-in-abuja";
            case "ibadan": return BASE_URL + "/hotels-in-oyo";
            case "kano": return BASE_URL + "/hotels-in-kano";
            case "lagos": return BASE_URL + "/hotels-in-lagos";
            case "jos": return BASE_URL + "/hotels-in-plateau";
            case "calabar": return BASE_URL + "/hotels-in-crossriver";
            case "warri": return BASE_URL + "/hotels-in-delta";
            case "benin": return BASE_URL + "/hotels-in-edo";
            case "kaduna": return BASE_URL + "/hotels-in-kaduna";
            case "enugu": return BASE_URL + "/hotels-in-enugu";
            default: return BASE_URL + "/hotels-in-" + citySlug;
        }
    }

    private Elements extractHotelElements(Document doc) {
        Elements hotels = doc.select("div[class*='hotel'], .hotel-item, div[class*='property']");
        if (hotels.isEmpty()) hotels = doc.select("div:contains(₦):has(a), div:contains(Naira):has(a)");
        if (hotels.isEmpty()) hotels = doc.select("div:matches(.*Hotel.*|.*Resort.*|.*Inn.*|.*Lodge.*):has(a)");
        if (hotels.isEmpty()) hotels = doc.select("div:has(a[href*='hotel'])");
        return hotels;
    }

    private Hotels extractHotelFromElement(Element element, String city, String country, Integer date) {
        try {
            Hotels hotel = new Hotels();
            hotel.setId(UUID.randomUUID().toString());

            String hotelName = extractHotelName(element);
            if (hotelName.trim().isEmpty()) return null;
            hotel.setHotelName(hotelName.trim());

            hotel.setPrice(extractPrice(element));
            hotel.setAddress(extractAddress(element));
            hotel.setUrl(extractUrl(element));
            hotel.setCity(city != null ? city : "N/A");
            hotel.setCountry(country != null ? country : "Nigeria");
            hotel.setType("Hotel");
            hotel.setDate(date != null ? date.toString() : "N/A");

            StringBuilder description = new StringBuilder("Hotel in ");
            description.append(hotel.getCity()).append(", Nigeria");
            if (hotel.getAddress() != null && !hotel.getAddress().equals("N/A")) {
                description.append(" - ").append(hotel.getAddress());
            }
            hotel.setHotelDescription(description.toString());

            return hotel;
        } catch (Exception e) {
            throw new HotelsNotFoundException("Hotels fot Found but scraping passed");
        }
    }

    private String extractHotelName(Element element) {
        String[] nameSelectors = {"h1", "h2", "h3", "h4", ".hotel-name", ".property-name", ".title", "a[title]", "strong", "b"};

        for (String selector : nameSelectors) {
            Element nameElement = element.select(selector).first();
            if (nameElement != null && !nameElement.text().trim().isEmpty()) {
                String name = nameElement.text().trim();
                if (!name.toLowerCase().matches(".*book.*|.*search.*|.*filter.*|.*sort.*|.*₦.*only")) {
                    return name;
                }
            }
        }

        Elements links = element.select("a[href*='hotel']");
        for (Element link : links) {
            String linkText = link.text().trim();
            if (linkText.length() > 3) {
                return linkText;
            }
        }
        return "N/A";
    }

    private Long extractPrice(Element element) {
        Pattern pricePattern = Pattern.compile("₦([0-9,]+)");
        String elementText = element.text();
        Matcher matcher = pricePattern.matcher(elementText);

        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        Pattern altPricePattern = Pattern.compile("([0-9,]+)\\s*(?:naira|NGN|₦)");
        matcher = altPricePattern.matcher(elementText.toLowerCase());
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return 0L;
    }


    private String extractAddress(Element element) {
        String text = element.text();
        Pattern addressPattern = Pattern.compile("(?:No\\.?\\s*)?\\d+[^,]+(?:Street|Road|Avenue|Close|Crescent|Drive)[^,]*(?:,\\s*[^,]*)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = addressPattern.matcher(text);

        if (matcher.find()) return matcher.group().trim();

        String[] areas = {"Victoria Island", "Ikeja", "Lekki", "Ikoyi", "Surulere", "Yaba", "Gbagada", "Maryland"};
        for (String area : areas) {
            if (text.contains(area)) {
                int index = text.indexOf(area);
                int start = Math.max(0, index - 50);
                int end = Math.min(text.length(), index + area.length() + 50);
                return text.substring(start, end).trim();
            }
        }
        return "N/A";
    }
    private String extractUrl(Element element) {
        Elements links = element.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if (href.contains("hotel") || href.contains("booking")) {
                if (href.startsWith("/")) return BASE_URL + href;
                if (href.startsWith("http")) return href;
            }
        }
        return "N/A";
    }
}
