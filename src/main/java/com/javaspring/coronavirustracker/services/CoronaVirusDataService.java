package com.javaspring.coronavirustracker.services;


import com.javaspring.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;


@Service
public class CoronaVirusDataService {

    private static String CORONA_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private ArrayList<LocationStats> allStats;

    @PostConstruct
    @Scheduled(cron = "0 0 * ? * *")
    public void fetchCoronaData() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CORONA_DATA_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(response.body());
        StringReader in = new StringReader(response.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        ArrayList<LocationStats> newStats = new ArrayList<>();
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setState(record.get("Province/State"));


            int prevDay = Integer.parseInt(record.get(record.size()-2));
            int actualDay = (!record.get(record.size() - 1).isEmpty()) ?  Integer.parseInt(record.get(record.size() - 1)) :  prevDay ;

            locationStats.setAddedSincePrevDay(actualDay-prevDay);
            locationStats.setLatestTotalCases(actualDay);
            //System.out.println(locationStats);
            newStats.add(locationStats);
        }
        this.allStats = newStats;
    }

    public ArrayList<LocationStats> getAllStats() {
        return allStats;
    }
}