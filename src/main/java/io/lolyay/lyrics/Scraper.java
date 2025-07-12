package io.lolyay.lyrics;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Scraper {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/244.178.44.111 Safari/537.36";

    public static String getSiteHTML(String url, Map<String, String> cookies) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Cookie", String.join("; ", cookies.entrySet().stream().map(Map.Entry::getKey).map(k -> k + "=" + cookies.get(k)).toList()))
                .build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
