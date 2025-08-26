package com.getgoods.placedetails.scraper;

import com.getgoods.placedetails.model.TouristAttraction;
import com.getgoods.placedetails.model.Food;
import com.getgoods.placedetails.model.Culture;
import com.getgoods.placedetails.model.PlaceDetails;
import com.getgoods.placedetails.util.ParsingException;
import com.getgoods.placedetails.util.PlaceDetailsNotFoundException;
import com.getgoods.placedetails.util.ScrapingException;
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

@Service
public class PlaceDetailsScraper {
    // DO NOT FORGET TO REMOVE SAVE

















    private static final String WIKIVOYAGE_BASE_URL = "https://en.wikivoyage.org/wiki/";
    private static final String FOURSQUARE_BASE_URL = "https://foursquare.com/explore?mode=url&near=";
    private static final int TIMEOUT = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int MAX_ITEMS_PER_CATEGORY = 12;

    public PlaceDetails findPlaceDetails(String city, String country) {
        PlaceDetails placeDetails = new PlaceDetails();
        placeDetails.setCity(city);
        placeDetails.setCountry(country);

        try {
            scrapeFromWikivoyage(placeDetails, city, country);
            if (isEmpty(placeDetails)) {
                scrapeFromFoursquare(placeDetails, city, country);
            }
            if (isEmpty(placeDetails)) {
                throw new PlaceDetailsNotFoundException("No relevant data found for " + city);
            }

        } catch (PlaceDetailsNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape place details for " + city + ": " + e.getMessage());
        }

        return placeDetails;
    }

    private void scrapeFromWikivoyage(PlaceDetails placeDetails, String city, String country) throws Exception {
        String wikivoyageUrl = buildWikivoyageUrl(city, country);
        Document doc = Jsoup.connect(wikivoyageUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .followRedirects(true)
                .get();

        List<TouristAttraction> attractions = extractTouristAttractions(doc, city);
        List<Food> foods = extractFood(doc, city);
        List<Culture> cultures = extractCulture(doc, city);

        placeDetails.setTouristAttractions(attractions);
        placeDetails.setFoods(foods);
        placeDetails.setCultures(cultures);
    }

    private void scrapeFromFoursquare(PlaceDetails placeDetails, String city, String country) {
        try {
            String foursquareUrl = FOURSQUARE_BASE_URL + city.replace(" ", "%20") + ",%20" + country.replace(" ", "%20");
            Document doc = Jsoup.connect(foursquareUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            List<TouristAttraction> attractions = extractFoursquareAttractions(doc, city);
            List<Food> foods = extractFoursquareFood(doc, city);

            if (!attractions.isEmpty()) placeDetails.setTouristAttractions(attractions);
            if (!foods.isEmpty()) placeDetails.setFoods(foods);

        } catch (Exception e) {
            throw new ScrapingException("Scraping Failed for " + city + ": " + e.getMessage());
        }
    }

    private boolean isEmpty(PlaceDetails placeDetails) {
        return (placeDetails.getTouristAttractions() == null || placeDetails.getTouristAttractions().isEmpty()) &&
                (placeDetails.getFoods() == null || placeDetails.getFoods().isEmpty()) &&
                (placeDetails.getCultures() == null || placeDetails.getCultures().isEmpty());
    }

    private String buildWikivoyageUrl(String city, String country) {
        String citySlug = city.trim()
                .replace(" ", "_")
                .replace("'", "%27")
                .replace("&", "%26");
        String countrySlug = country.trim().replace(" ", "_");

        List<String> urlVariations = new ArrayList<>();
        urlVariations.add(WIKIVOYAGE_BASE_URL + citySlug);
        urlVariations.add(WIKIVOYAGE_BASE_URL + citySlug + "_(" + countrySlug + ")");
        urlVariations.add(WIKIVOYAGE_BASE_URL + citySlug + ",_" + countrySlug);

        for (String url : urlVariations) {
            try {
                Document testDoc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(5000).get();
                if (!testDoc.title().toLowerCase().contains("not found")) {
                    return url;
                }
            } catch (Exception e) {
                throw new  ScrapingException("Scraping Failed for " + city + ": " + e.getMessage());
            }
        }

        return WIKIVOYAGE_BASE_URL + citySlug;
    }

    private List<TouristAttraction> extractTouristAttractions(Document doc, String city) {
        List<TouristAttraction> attractions = new ArrayList<>();
        Set<String> attractionNames = new HashSet<>();

        Elements seeSection = findWikivoyageSection(doc, "See");
        Elements doSection = findWikivoyageSection(doc, "Do");

        attractions.addAll(extractAttractionsFromElements(seeSection, city, attractionNames));
        attractions.addAll(extractAttractionsFromElements(doSection, city, attractionNames));
        if (attractions.isEmpty()) {
            Elements alternativeItems = doc.select("div.vcard, .listing, h3 + ul li, h2 + ul li");
            attractions.addAll(extractAttractionsFromElements(alternativeItems, city, attractionNames));
        }

        return attractions.subList(0, Math.min(attractions.size(), MAX_ITEMS_PER_CATEGORY));
    }

    private List<Food> extractFood(Document doc, String city) {
        List<Food> foods = new ArrayList<>();
        Set<String> foodNames = new HashSet<>();
        Elements eatSection = findWikivoyageSection(doc, "Eat");
        foods.addAll(extractFoodFromElements(eatSection, city, foodNames));

        return foods.subList(0, Math.min(foods.size(), MAX_ITEMS_PER_CATEGORY));
    }

    private List<Culture> extractCulture(Document doc, String city) {
        List<Culture> cultures = new ArrayList<>();
        Set<String> cultureNames = new HashSet<>();
        Elements understandSection = findWikivoyageSection(doc, "Understand");
        Elements cultureSection = findWikivoyageSection(doc, "Culture");
        Elements historySection = findWikivoyageSection(doc, "History");

        cultures.addAll(extractCultureFromElements(understandSection, city, cultureNames));
        cultures.addAll(extractCultureFromElements(cultureSection, city, cultureNames));
        cultures.addAll(extractCultureFromElements(historySection, city, cultureNames));

        return cultures.subList(0, Math.min(cultures.size(), MAX_ITEMS_PER_CATEGORY));
    }

    private Elements findWikivoyageSection(Document doc, String sectionName) {
        Elements items = new Elements();
        Elements headings = doc.select("h2, h3, h4");
        Element targetHeading = null;

        for (Element heading : headings) {
            if (heading.text().toLowerCase().contains(sectionName.toLowerCase())) {
                targetHeading = heading;
                break;
            }
        }

        if (targetHeading != null) {
            Element sectionElement = targetHeading.closest("section");
            if (sectionElement == null) {
                Element current = targetHeading.nextElementSibling();
                while (current != null && !current.tagName().matches("h[2-4]")) {
                    items.addAll(extractItemsFromElement(current));
                    current = current.nextElementSibling();
                }
            } else {
                items.addAll(extractItemsFromElement(sectionElement));
            }

        }

        return items;
    }

    private Elements extractItemsFromElement(Element element) {
        Elements items = new Elements();

        items.addAll(element.select("bdi.vcard"));
        items.addAll(element.select("li bdi.vcard"));
        items.addAll(element.select("ul > li"));
        items.addAll(element.select("dl > dd"));
        items.addAll(element.select("div.listing"));
        items.addAll(element.select("p"));
        Elements filteredItems = new Elements();
        for (Element item : items) {
            String text = item.text().trim();

            if (text.length() < 20 || text.matches("^\\d+\\.\\s*\\d+.*$")) {
                continue;
            }

            if (text.matches("^\\d+$") || text.matches("^\\d+[a-z]*$")) {
                continue;
            }
            boolean isDuplicate = false;
            for (Element existing : filteredItems) {
                String existingText = existing.text().trim();
                if (existingText.equals(text) ||
                        (existingText.length() > 50 && text.length() > 50 &&
                                existingText.substring(0, 50).equals(text.substring(0, 50)))) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    private boolean containsElement(Elements elements, Element target) {
        String targetText = target.text().trim();
        for (Element existing : elements) {
            if (existing.text().trim().equals(targetText)) {
                return true;
            }
        }
        return false;
    }

    private List<TouristAttraction> extractAttractionsFromElements(Elements elements, String city, Set<String> names) {
        List<TouristAttraction> attractions = new ArrayList<>();

        for (Element element : elements) {
            if (attractions.size() >= MAX_ITEMS_PER_CATEGORY) break;

            String text = element.text().trim();
            if (text.length() < 10) continue;

            String name = extractNameFromElement(element);
            if (name != null && names.add(name.toLowerCase())) {
                TouristAttraction attraction = new TouristAttraction();
                attraction.setId(UUID.randomUUID().toString());
                attraction.setName(name);
                attraction.setDescription(truncateDescription(text));
                attraction.setCity(city);
                attraction.setCategory(extractAttractionCategory(text));
                attraction.setLocation(extractLocationFromText(element));

                attractions.add(attraction);
            }
        }

        return attractions;
    }

    private List<Food> extractFoodFromElements(Elements elements, String city, Set<String> names) {
        List<Food> foods = new ArrayList<>();

        for (Element element : elements) {
            if (foods.size() >= MAX_ITEMS_PER_CATEGORY) break;

            String text = element.text().trim();
            if (text.length() < 10) continue;

            String name = extractNameFromElement(element);
            if (name != null && names.add(name.toLowerCase())) {
                Food food = new Food();
                food.setId(UUID.randomUUID().toString());
                food.setName(name);
                food.setDescription(truncateDescription(text));
                food.setCity(city);
                food.setCuisineType(extractCuisineType(text));
                food.setPriceRange(extractPriceRange(text));

                foods.add(food);
            }
        }

        return foods;
    }

    private List<Culture> extractCultureFromElements(Elements elements, String city, Set<String> names) {
        List<Culture> cultures = new ArrayList<>();

        for (Element element : elements) {
            if (cultures.size() >= MAX_ITEMS_PER_CATEGORY) break;

            String text = element.text().trim();
            if (text.length() < 10) continue;

            String name = extractNameFromElement(element);
            if (name != null && names.add(name.toLowerCase())) {
                Culture culture = new Culture();
                culture.setId(UUID.randomUUID().toString());
                culture.setName(name);
                culture.setDescription(truncateDescription(text));
                culture.setCity(city);
                culture.setCategory(extractCulturalCategory(text));
                culture.setHistoricalPeriod(extractHistoricalPeriod(text));

                cultures.add(culture);
            }
        }

        return cultures;
    }

    private List<TouristAttraction> extractFoursquareAttractions(Document doc, String city) {
        List<TouristAttraction> attractions = new ArrayList<>();
        Set<String> names = new HashSet<>();

        Elements venueElements = doc.select(".venue, .venueItem, [data-venue-id]");

        for (Element venue : venueElements) {
            if (attractions.size() >= MAX_ITEMS_PER_CATEGORY) break;

            String name = venue.select(".venueName, .venue-name, h3, h4").text();
            String category = venue.select(".categoryName, .venue-category").text();
            String rating = venue.select(".rating, .score").text();

            if (!name.isEmpty() && names.add(name.toLowerCase())) {
                TouristAttraction attraction = new TouristAttraction();
                attraction.setId(UUID.randomUUID().toString());
                attraction.setName(name);
                attraction.setDescription("Rating: " + rating + " - " + category);
                attraction.setCity(city);
                attraction.setCategory(category.isEmpty() ? "Attraction" : category);
                attraction.setLocation(city);

                attractions.add(attraction);
            }
        }

        return attractions;
    }

    private List<Food> extractFoursquareFood(Document doc, String city) {
        List<Food> foods = new ArrayList<>();
        Set<String> names = new HashSet<>();

        Elements allVenues = doc.select(".venue, .venueItem");
        Elements restaurantElements = new Elements();

        for (Element venue : allVenues) {
            String text = venue.text().toLowerCase();
            if (text.contains("restaurant") || text.contains("food") ||
                    text.contains("cafe") || text.contains("dining") ||
                    text.contains("bar") || text.contains("kitchen")) {
                restaurantElements.add(venue);
            }
        }

        for (Element restaurant : restaurantElements) {
            if (foods.size() >= MAX_ITEMS_PER_CATEGORY) break;

            String name = restaurant.select(".venueName, .venue-name").text();
            String category = restaurant.select(".categoryName, .venue-category").text();

            if (!name.isEmpty() && names.add(name.toLowerCase())) {
                Food food = new Food();
                food.setId(UUID.randomUUID().toString());
                food.setName(name);
                food.setDescription(category);
                food.setCity(city);
                food.setCuisineType(extractCuisineType(category));
                food.setPriceRange("Moderate");

                foods.add(food);
            }
        }

        return foods;
    }

    private String extractNameFromElement(Element element) {
        if (element.hasClass("vcard")) {
            String dataMw = element.attr("data-mw");
            if (!dataMw.isEmpty()) {
                try {
                    if (dataMw.contains("\"name\":{\"wt\":\"")) {
                        String namePattern = "\"name\":\\{\"wt\":\"([^\"]+)\"";
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(namePattern);
                        java.util.regex.Matcher matcher = pattern.matcher(dataMw);
                        if (matcher.find()) {
                            String name = matcher.group(1);
                            name = name.replaceAll("\\[\\[.*?\\|(.*?)\\]\\]", "$1");
                            name = name.replaceAll("\\[\\[(.*?)#.*?\\]\\]", "$1");
                            name = name.replaceAll("\\[\\[(.*?)\\]\\]", "$1");
                            name = name.trim();
                            if (name.length() > 2 && name.length() < 100 && !name.matches("\\d+.*")) {
                                return name;
                            }
                        }
                    }

                    if (dataMw.contains("\"alt\":{\"wt\":\"")) {
                        String altPattern = "\"alt\":\\{\"wt\":\"([^\"]+)\"";
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(altPattern);
                        java.util.regex.Matcher matcher = pattern.matcher(dataMw);
                        if (matcher.find()) {
                            String alt = matcher.group(1).trim();
                            if (alt.length() > 2 && alt.length() < 100 && !alt.matches("\\d+.*")) {
                                return alt;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new ParsingException("Error parsing data-mw: " + e.getMessage());
                }
            }
        }
        Element nameSpan = element.selectFirst(".fn, .p-name");
        if (nameSpan != null && !nameSpan.text().trim().isEmpty()) {
            String name = nameSpan.text().trim();
            if (!name.matches("\\d+\\..*")) {
                return name;
            }
        }

        Elements links = element.select("a");
        if (!links.isEmpty()) {
            String linkText = links.first().text().trim();
            if (linkText.length() > 2 && linkText.length() < 80 && !linkText.matches("\\d+\\..*")) {
                return linkText;
            }
        }

        Elements boldElements = element.select("b, strong");
        if (!boldElements.isEmpty()) {
            String boldText = boldElements.first().text().trim();
            if (boldText.length() > 2 && boldText.length() < 80 && !boldText.matches("\\d+\\..*")) {
                return boldText;
            }
        }

        String text = element.text().trim();

        if (text.matches("^\\d+\\.\\s*\\d+.*")) {
            return null;
        }

        if (text.contains("—")) {
            String[] parts = text.split("—", 2);
            String name = parts[0].trim();
            if (name.length() > 2 && name.length() < 80 && !name.matches("\\d+\\..*")) {
                return name;
            }
        }

        if (text.contains(":") && !text.startsWith("Tel:") && !text.startsWith("Phone:")) {
            String[] parts = text.split(":", 2);
            String name = parts[0].trim();
            if (name.length() > 2 && name.length() < 80 && !name.matches("\\d+\\..*")) {
                return name;
            }
        }

        if (text.contains(".") && text.length() > 20) {
            String[] sentences = text.split("\\.", 2);
            String firstSentence = sentences[0].trim();
            if (firstSentence.length() > 5 && firstSentence.length() < 80 &&
                    !firstSentence.matches("\\d+\\..*") && !firstSentence.matches("^\\d+$")) {
                return firstSentence;
            }
        }

        if (text.length() > 10 && !text.matches("^\\d+.*")) {
            return text.substring(0, Math.min(text.length(), 60)).trim() + "...";
        }

        return null;
    }

    private String truncateDescription(String text) {
        if (text.matches("^\\d+\\.\\s*\\d+.*$")) {
            return "Description not available";
        }

        text = text.replaceAll("\\[\\[.*?\\|(.*?)\\]\\]", "$1");
        text = text.replaceAll("\\[\\[(.*?)\\]\\]", "$1");

        if (text.contains(".") && text.length() > 30) {
            String[] sentences = text.split("\\.", 3);
            StringBuilder result = new StringBuilder(sentences[0].trim());

            if (sentences.length > 1 && sentences[1].trim().length() > 10 && result.length() < 100) {
                result.append(". ").append(sentences[1].trim());
            }

            String finalResult = result.toString();
            return finalResult.length() > 200 ? finalResult.substring(0, 197) + "..." : finalResult;
        }

        return text.length() > 200 ? text.substring(0, 197) + "..." : text;
    }

    private String extractLocationFromText(Element element) {
        if (element.hasClass("vcard")) {
            String dataMw = element.attr("data-mw");
            if (!dataMw.isEmpty()) {
                if (dataMw.contains("\"address\":{\"wt\":\"")) {
                    String addressPattern = "\"address\":\\{\"wt\":\"([^\"]+)\"";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(addressPattern);
                    java.util.regex.Matcher matcher = pattern.matcher(dataMw);
                    if (matcher.find()) {
                        String address = matcher.group(1).trim();
                        if (address.length() > 5 && !address.matches("\\d+\\..*")) {
                            return address;
                        }
                    }
                }

                if (dataMw.contains("\"directions\":{\"wt\":\"")) {
                    String directionsPattern = "\"directions\":\\{\"wt\":\"([^\"]+)\"";
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(directionsPattern);
                    java.util.regex.Matcher matcher = pattern.matcher(dataMw);
                    if (matcher.find()) {
                        String directions = matcher.group(1).trim();
                        if (directions.length() > 5) {
                            return directions;
                        }
                    }
                }
            }
        }

        Element addressSpan = element.selectFirst(".adr, .p-adr, .address");
        if (addressSpan != null) {
            return addressSpan.text().trim();
        }

        String text = element.text();
        String[] locationKeywords = {"located", "situated", "address:", "metro:", "métro:", "near", "@"};

        for (String keyword : locationKeywords) {
            int index = text.toLowerCase().indexOf(keyword);
            if (index != -1) {
                String locationPart = text.substring(index + keyword.length()).trim();
                if (locationPart.contains(".")) {
                    locationPart = locationPart.split("\\.")[0];
                }
                if (locationPart.contains(",")) {
                    locationPart = locationPart.split(",")[0];
                }
                if (locationPart.length() > 5 && locationPart.length() < 100 &&
                        !locationPart.matches("\\d+\\..*")) {
                    return locationPart.trim();
                }
            }
        }

        return "Location not specified";
    }

    private String extractCuisineType(String text) {
        String[] cuisineTypes = {"traditional", "local", "street food", "fine dining",
                "fast food", "seafood", "vegetarian", "continental", "italian",
                "chinese", "indian", "mexican", "french", "japanese"};
        String lowerText = text.toLowerCase();
        for (String cuisine : cuisineTypes) {
            if (lowerText.contains(cuisine)) return cuisine;
        }
        return "Local Cuisine";
    }

    private String extractPriceRange(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("expensive") || lowerText.contains("costly") || lowerText.contains("$$$")) return "Expensive";
        if (lowerText.contains("cheap") || lowerText.contains("affordable") || lowerText.contains("$")) return "Affordable";
        if (lowerText.contains("budget")) return "Budget";
        if (lowerText.contains("$$")) return "Moderate";
        return "Moderate";
    }

    private String extractCulturalCategory(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("museum")) return "Museum";
        if (lowerText.contains("festival")) return "Festival";
        if (lowerText.contains("temple") || lowerText.contains("church") || lowerText.contains("mosque")) return "Religious Site";
        if (lowerText.contains("art") || lowerText.contains("gallery")) return "Art & Gallery";
        if (lowerText.contains("theater") || lowerText.contains("theatre")) return "Theatre";
        if (lowerText.contains("monument") || lowerText.contains("memorial")) return "Monument";
        if (lowerText.contains("palace") || lowerText.contains("fort")) return "Historical Building";
        return "Cultural Site";
    }

    private String extractHistoricalPeriod(String text) {
        if (text.matches(".*\\b(19|20)\\d{2}\\b.*")) return "Modern Era";
        if (text.matches(".*\\b(16|17|18)\\d{2}\\b.*")) return "Colonial Era";
        String lowerText = text.toLowerCase();
        if (lowerText.contains("ancient")) return "Ancient";
        if (lowerText.contains("medieval")) return "Medieval";
        if (lowerText.contains("renaissance")) return "Renaissance";
        return "Historical";
    }

    private String extractAttractionCategory(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("museum")) return "Museum";
        if (lowerText.contains("park")) return "Park";
        if (lowerText.contains("monument") || lowerText.contains("memorial")) return "Monument";
        if (lowerText.contains("temple") || lowerText.contains("church") || lowerText.contains("mosque")) return "Religious Site";
        if (lowerText.contains("event") || lowerText.contains("festival")) return "Event";
        if (lowerText.contains("market") || lowerText.contains("shopping")) return "Shopping";
        if (lowerText.contains("beach")) return "Beach";
        if (lowerText.contains("building") || lowerText.contains("architecture")) return "Architecture";
        return "Landmark";
    }

}