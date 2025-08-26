package com.getgoods.placedetails.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewScraper {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 10000;

    public static class Review {
        private String author;
        private String text;
        private String rating;
        private String date;

        // Constructors, getters, setters
        public Review(String author, String text, String rating, String date) {
            this.author = author;
            this.text = text;
            this.rating = rating;
            this.date = date;
        }

        // Getters and setters...
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getRating() { return rating; }
        public void setRating(String rating) { this.rating = rating; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public List<Review> scrapeReviews(String city, String country) {
        List<Review> allReviews = new ArrayList<>();

        // Try multiple sources
        allReviews.addAll(scrapeFromYelp(city, country));
        allReviews.addAll(scrapeFromFoursquare(city, country));

        return allReviews.subList(0, Math.min(allReviews.size(), 20));
    }

    private List<Review> scrapeFromYelp(String city, String country) {
        List<Review> reviews = new ArrayList<>();

        try {
            String yelpUrl = "https://www.yelp.com/search?find_desc=attractions&find_loc=" +
                    city.replace(" ", "+") + "%2C+" + country.replace(" ", "+");

            Document doc = Jsoup.connect(yelpUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .referrer("https://www.google.com/")
                    .get();

            // Extract businesses first
            Elements businesses = doc.select("[data-testid='serp-ia-card'], .businessName, .biz-name");

            for (Element business : businesses.subList(0, Math.min(businesses.size(), 5))) {
                String businessName = business.select("h3, h4, .biz-name").text();
                if (businessName.isEmpty()) continue;

                // Try to get individual business page for reviews
                Elements links = business.select("a[href*='/biz/']");
                if (!links.isEmpty()) {
                    String businessUrl = "https://www.yelp.com" + links.first().attr("href");
                    reviews.addAll(scrapeYelpBusinessReviews(businessUrl, businessName));
                }

                if (reviews.size() >= 15) break;
            }

        } catch (Exception e) {
            System.out.println("Yelp scraping failed: " + e.getMessage());
        }

        return reviews;
    }

    private List<Review> scrapeYelpBusinessReviews(String businessUrl, String businessName) {
        List<Review> reviews = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(businessUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            // Look for review elements
            Elements reviewElements = doc.select("[data-testid='reviews-section'] li, .review, .review-item");

            for (Element reviewEl : reviewElements.subList(0, Math.min(reviewElements.size(), 3))) {
                String author = reviewEl.select(".user-name, .reviewer-name, [data-testid='name']").text();
                String text = reviewEl.select(".review-text, .comment, p").text();
                String rating = extractRating(reviewEl);
                String date = reviewEl.select(".date, .review-date, time").text();

                if (!text.isEmpty() && text.length() > 20) {
                    reviews.add(new Review(
                            author.isEmpty() ? "Anonymous" : author,
                            businessName + ": " + truncateText(text, 200),
                            rating,
                            date
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to scrape business reviews from: " + businessUrl);
        }

        return reviews;
    }

    private List<Review> scrapeFromFoursquare(String city, String country) {
        List<Review> reviews = new ArrayList<>();

        try {
            String foursquareUrl = "https://foursquare.com/explore?mode=url&near=" +
                    city.replace(" ", "%20") + ",%20" + country.replace(" ", "%20");

            Document doc = Jsoup.connect(foursquareUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements venues = doc.select(".venue, .venueItem, [data-venue-id]");

            for (Element venue : venues.subList(0, Math.min(venues.size(), 10))) {
                String venueName = venue.select(".venueName, .venue-name, h3").text();
                String category = venue.select(".categoryName, .venue-category").text();
                String rating = venue.select(".rating, .score, .venue-rating").text();
                String tips = venue.select(".tip, .venue-tip, .comment").text();

                if (!tips.isEmpty() && tips.length() > 20) {
                    reviews.add(new Review(
                            "Foursquare User",
                            venueName + " (" + category + "): " + truncateText(tips, 180),
                            rating.isEmpty() ? "N/A" : rating,
                            "Recent"
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Foursquare review scraping failed: " + e.getMessage());
        }

        return reviews;
    }

    // Alternative: Simple local tourism websites
    public List<Review> scrapeFromLocalSites(String city, String country) {
        List<Review> reviews = new ArrayList<>();

        try {
            // Try generic search for city + reviews
            String searchQuery = city + " " + country + " tourist attractions reviews";
            String googleSearchUrl = "https://www.google.com/search?q=" +
                    searchQuery.replace(" ", "+") + "&tbm=";

            Document doc = Jsoup.connect(googleSearchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            // Look for review snippets in search results
            Elements searchResults = doc.select(".g, .search-result");

            for (Element result : searchResults.subList(0, Math.min(searchResults.size(), 5))) {
                String snippet = result.select(".st, .snippet, .description").text();
                String title = result.select("h3, .title").text();

                if (!snippet.isEmpty() && snippet.length() > 50 &&
                        (snippet.toLowerCase().contains("review") ||
                                snippet.toLowerCase().contains("visit") ||
                                snippet.toLowerCase().contains("experience"))) {

                    reviews.add(new Review(
                            "Travel Review",
                            title + ": " + truncateText(snippet, 200),
                            "N/A",
                            "Recent"
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Local sites scraping failed: " + e.getMessage());
        }

        return reviews;
    }

    // Helper method to extract rating from various formats
    private String extractRating(Element element) {
        // Look for star ratings
        Elements stars = element.select(".stars, .rating, [aria-label*='star'], [title*='star']");
        if (!stars.isEmpty()) {
            String starText = stars.first().attr("aria-label");
            if (starText.isEmpty()) starText = stars.first().attr("title");
            if (starText.isEmpty()) starText = stars.first().text();

            // Extract number from star rating
            if (starText.matches(".*\\b[1-5](\\.\\d)?\\b.*")) {
                return starText.replaceAll(".*\\b([1-5](?:\\.\\d)?)\\b.*", "$1") + "/5";
            }
        }

        // Look for numeric ratings
        Elements ratingElements = element.select("[class*='rating'], [class*='score']");
        for (Element ratingEl : ratingElements) {
            String ratingText = ratingEl.text();
            if (ratingText.matches(".*\\b\\d+(\\.\\d+)?\\b.*")) {
                return ratingText.replaceAll(".*\\b(\\d+(?:\\.\\d+)?)\\b.*", "$1");
            }
        }

        return "N/A";
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;

        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > maxLength - 50) {
            truncated = truncated.substring(0, lastSpace);
        }
        return truncated + "...";
    }

    // Alternative simple review scraper for TripAdvisor (use carefully)
    public List<Review> scrapeFromTripAdvisor(String city, String country) {
        List<Review> reviews = new ArrayList<>();

        try {
            // Search for city attractions on TripAdvisor
            String searchUrl = "https://www.tripadvisor.com/Search?q=" +
                    city.replace(" ", "%20") + "%20" + country.replace(" ", "%20") +
                    "&searchSessionId=&ssrc=e&geo=1";

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(TIMEOUT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .referrer("https://www.google.com/")
                    .get();

            // Look for attraction listings
            Elements attractions = doc.select(".result-title, .listing_title, [data-test-target]");

            for (Element attraction : attractions.subList(0, Math.min(attractions.size(), 8))) {
                String attractionName = attraction.text();
                if (attractionName.length() < 5) continue;

                // Look for review snippets in the same container
                Element container = attraction.parent();
                if (container != null) {
                    Elements reviewElements = container.select(".review-text, .partial_entry, .entry");
                    String reviewText = reviewElements.isEmpty() ? "" : reviewElements.first().text();

                    Elements ratingElements = container.select(".ui_bubble_rating, .rating, [class*='bubble']");
                    String rating = ratingElements.isEmpty() ? "N/A" : extractTripAdvisorRating(ratingElements.first());

                    if (!reviewText.isEmpty() && reviewText.length() > 30) {
                        reviews.add(new Review(
                                "TripAdvisor User",
                                attractionName + ": " + truncateText(reviewText, 180),
                                rating,
                                "Recent"
                        ));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("TripAdvisor scraping failed: " + e.getMessage());
        }

        return reviews;
    }

    private String extractTripAdvisorRating(Element ratingElement) {
        String className = ratingElement.className();
        if (className.contains("bubble_")) {
            // Extract rating from bubble class like "bubble_50" = 5.0 stars
            String ratingClass = className.replaceAll(".*bubble_(\\d+).*", "$1");
            if (ratingClass.matches("\\d+")) {
                double rating = Double.parseDouble(ratingClass) / 10.0;
                return String.format("%.1f/5", rating);
            }
        }
        return "N/A";
    }

    // Method to combine all review sources
    public List<Review> getAllReviews(String city, String country) {
        List<Review> allReviews = new ArrayList<>();

        System.out.println("Scraping reviews for " + city + ", " + country);

        // Try multiple sources
        try {
            allReviews.addAll(scrapeFromFoursquare(city, country));
            Thread.sleep(1000); // Be respectful with requests
        } catch (Exception e) {
            System.out.println("Foursquare failed: " + e.getMessage());
        }

        try {
            allReviews.addAll(scrapeFromYelp(city, country));
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Yelp failed: " + e.getMessage());
        }

        try {
            allReviews.addAll(scrapeFromLocalSites(city, country));
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Local sites failed: " + e.getMessage());
        }

        // Only try TripAdvisor if we have very few reviews
        if (allReviews.size() < 5) {
            try {
                allReviews.addAll(scrapeFromTripAdvisor(city, country));
            } catch (Exception e) {
                System.out.println("TripAdvisor failed: " + e.getMessage());
            }
        }

        System.out.println("Total reviews found: " + allReviews.size());
        return allReviews.subList(0, Math.min(allReviews.size(), 25));
    }
}