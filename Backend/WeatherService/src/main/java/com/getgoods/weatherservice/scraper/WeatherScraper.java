package com.getgoods.weatherservice.scraper;

import com.getgoods.weatherservice.Model.Weather;
import com.getgoods.weatherservice.Model.WeatherId;
import com.getgoods.weatherservice.util.WeatherNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class WeatherScraper {

    public Weather findByWeatherId(WeatherId weatherId) {
        try {
            LocalDate now = LocalDate.now();
            int dayOfMonth = Integer.parseInt(weatherId.getDate());
            LocalDate inputDate = LocalDate.of(now.getYear(), now.getMonth(), dayOfMonth);

            String city = weatherId.getCity().toLowerCase().replace(" ", "-");
            String country = weatherId.getCountry().toLowerCase().replace(" ", "-");
            String url = "https://www.timeanddate.com/weather/" + country + "/" + city + "/ext?lang=en";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15 * 1000)
                    .get();

            Element table = doc.selectFirst("table#wt-ext");
            if (table == null) {
                table = doc.selectFirst("table.zebra.tb-wt");
            }

            if (table != null) {
                Elements rows = table.select("tbody > tr");

                for (Element row : rows) {
                    Element dateHeader = row.selectFirst("th");
                    if (dateHeader != null) {
                        String headerText = dateHeader.text();
                        String dayText = extractDayFromHeader(headerText);

                        if (dayText.equals(String.valueOf(dayOfMonth))) {
                            Elements tds = row.select("td");

                            if (tds.size() >= 8) {
                                String tempText = tds.get(1).text();
                                String description = tds.get(2).text();
                                String feelsLike = tds.size() > 3 ? tds.get(3).text() : "";
                                String windSpeed = tds.size() > 4 ? tds.get(4).text() : "";
                                String humidity = tds.size() > 6 ? tds.get(6).text() : "";
                                String visibility = tds.size() > 7 ? tds.get(7).text() : "";
                                String precipitation = tds.size() > 8 ? tds.get(8).text() : "";

                                // Parse temperature
                                String maxTemp = "N/A";
                                String minTemp = "N/A";
                                if (tempText.contains("/")) {
                                    String cleanTemp = tempText.replace("°C", "").replace("°F", "")
                                            .replace("&nbsp;", " ").trim();
                                    String[] temps = cleanTemp.split("/");
                                    if (temps.length >= 2) {
                                        maxTemp = temps[0].trim();
                                        minTemp = temps[1].trim();
                                    }
                                } else if (tempText.matches(".*\\d+.*")) {
                                    maxTemp = tempText.replaceAll("[^0-9.-]", "");
                                    minTemp = maxTemp;
                                }
                                //advise
                                StringBuilder travelDesc = new StringBuilder();
                                travelDesc.append(description);

                                if (!windSpeed.isEmpty()) {
                                    travelDesc.append(" | Wind: ").append(windSpeed);
                                }
                                if (!humidity.isEmpty()) {
                                    travelDesc.append(" | Humidity: ").append(humidity);
                                }
                                if (!visibility.isEmpty()) {
                                    travelDesc.append(" | Visibility: ").append(visibility);
                                }
                                if (!precipitation.isEmpty() && !precipitation.equals("0") && !precipitation.equals("0 mm")) {
                                    travelDesc.append(" | Rain: ").append(precipitation);
                                }

                                String travelAdvice = generateTravelAdvice(description, maxTemp, minTemp, precipitation, windSpeed);
                                if (!travelAdvice.isEmpty()) {
                                    travelDesc.append("Travel Tip: ").append(travelAdvice);
                                }

                                Weather weather = new Weather();
                                weather.setId(weatherId);
                                weather.setMaxTemp(maxTemp);
                                weather.setMinTemp(minTemp);
                                weather.setTravelAdvice(travelAdvice);
                                weather.setOthers(travelDesc.toString());
                                weather.setDay(inputDate.getDayOfWeek().toString());
                                weather.setDesc(description);
                                weather.setFeelsLike(feelsLike);
                                return weather;
                            }
                        }
                    }
                }
            }

            throw new WeatherNotFoundException("Weather data not found for " + weatherId.getCity() +
                    " on " + weatherId.getDate() + ". Date may be outside available forecast range.");

        } catch (NumberFormatException e) {
            throw new WeatherNotFoundException("Invalid date format. Expected day number (1-31), got: " + weatherId.getDate());
        } catch (Exception e) {
            throw new WeatherNotFoundException("Unable to fetch weather data for " + weatherId.toString() +
                    ". Error: " + e.getMessage());
        }
    }

    private String extractDayFromHeader(String headerText) {
        String[] parts = headerText.trim().split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d{1,2}")) {
                return part;
            }
        }
        return headerText.trim();
    }

    private String generateTravelAdvice(String description, String maxTemp, String minTemp,
                                        String precipitation, String windSpeed) {
        StringBuilder advice = new StringBuilder();

        try {
            int maxTempInt = Integer.parseInt(maxTemp);
            int minTempInt = Integer.parseInt(minTemp);

            // Temperature-based advice
            if (maxTempInt > 35) {
                advice.append("Very hot - carry water, sunscreen, light clothing");
            } else if (maxTempInt < 10) {
                advice.append("Cold - pack warm clothes, jackets");
            } else if (maxTempInt - minTempInt > 15) {
                advice.append("Large temperature variation - dress in layers");
            }

            // Weather condition advice
            if (description.toLowerCase().contains("rain") || description.toLowerCase().contains("shower")) {
                if (advice.length() > 0) advice.append(", ");
                advice.append("carry umbrella/raincoat");
            }

            if (description.toLowerCase().contains("fog")) {
                if (advice.length() > 0) advice.append(", ");
                advice.append("expect travel delays due to fog");
            }

            // Wind advice
            if (!windSpeed.isEmpty() && windSpeed.contains("km/h")) {
                String windValue = windSpeed.replaceAll("[^0-9]", "");
                if (!windValue.isEmpty() && Integer.parseInt(windValue) > 25) {
                    if (advice.length() > 0) advice.append(", ");
                    advice.append("windy conditions - secure loose items");
                }
            }

        } catch (NumberFormatException e) {

        }

        return advice.toString();
    }
}