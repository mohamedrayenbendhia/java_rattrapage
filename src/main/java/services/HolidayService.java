package services;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HolidayService {

    // la clé API que pour Calendarific
    private static final String API_KEY = "F2odPDmQ6t9QGSPMMTi50RUSXRbpkJPb";
    private static final String API_URL = "https://calendarific.com/api/v2/holidays";

    // Instance unique pour le pattern Singleton
    private static HolidayService instance;

    // Constructeur privé pour empêcher l'instanciation directe
    private HolidayService() {
        // Constructeur privé
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static HolidayService getInstance() {
        if (instance == null) {
            instance = new HolidayService();
        }
        return instance;
    }

    public List<String> getHolidays() throws IOException {
        String urlString = API_URL + "?api_key=" + API_KEY + "&country=TN&year=2025";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        List<Holiday> holidays = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("API request failed with response code: " + connection.getResponseCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray holidaysArray = jsonResponse.getJSONObject("response").getJSONArray("holidays");

            for (int i = 0; i < holidaysArray.length(); i++) {
                JSONObject holiday = holidaysArray.getJSONObject(i);
                String holidayName = holiday.getString("name");

                // Traduire les noms des jours fériés tunisiens en anglais
                String translatedName = translateHolidayName(holidayName);

                JSONObject dateObj = holiday.getJSONObject("date");
                int year = dateObj.getJSONObject("datetime").getInt("year");
                int month = dateObj.getJSONObject("datetime").getInt("month");
                int day = dateObj.getJSONObject("datetime").getInt("day");

                LocalDate holidayDate = LocalDate.of(year, month, day);
                
                // Ajouter seulement les jours fériés à partir d'aujourd'hui
                if (!holidayDate.isBefore(today)) {
                    holidays.add(new Holiday(translatedName, holidayDate));
                }
            }
        } catch (JSONException e) {
            System.err.println("Error parsing holiday data: " + e.getMessage());
            throw new IOException("Failed to parse holiday data", e);
        }

        // Trier par date et formater
        List<String> formattedHolidays = holidays.stream()
                .sorted((h1, h2) -> h1.date.compareTo(h2.date))
                .map(this::formatHoliday)
                .collect(Collectors.toList());

        return formattedHolidays;
    }

    /**
     * Traduit les noms des jours fériés tunisiens en anglais
     */
    private String translateHolidayName(String originalName) {
        Map<String, String> translations = new HashMap<>();
        
        // Jours fériés tunisiens en anglais
        translations.put("New Year's Day", "New Year's Day");
        translations.put("Independence Day", "Independence Day");
        translations.put("Youth Day", "Youth Day");
        translations.put("Martyrs' Day", "Martyrs' Day");
        translations.put("Labor Day", "Labor Day");
        translations.put("Republic Day", "Republic Day");
        translations.put("Women's Day", "Women's Day");
        translations.put("Revolution and Youth Day", "Revolution and Youth Day");
        translations.put("Evacuation Day", "Evacuation Day");
        
        // Jours fériés islamiques
        translations.put("Eid al-Fitr", "Eid al-Fitr");
        translations.put("Eid al-Adha", "Eid al-Adha");
        translations.put("Islamic New Year", "Islamic New Year");
        translations.put("Prophet Muhammad's Birthday", "Prophet Muhammad's Birthday");
        translations.put("Day of Arafat", "Day of Arafat");
        
        // Variantes possibles
        translations.put("Fête du Travail", "Labor Day");
        translations.put("Fête de l'Indépendance", "Independence Day");
        translations.put("Fête de la République", "Republic Day");
        translations.put("Jour de l'An", "New Year's Day");
        translations.put("Fête de la Jeunesse", "Youth Day");
        translations.put("Journée des Martyrs", "Martyrs' Day");
        
        return translations.getOrDefault(originalName, originalName);
    }

    private String formatHoliday(Holiday holiday) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        return holiday.name + " - " + holiday.date.format(formatter);
    }

    private static class Holiday {
        String name;
        LocalDate date;

        Holiday(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }
    }
}