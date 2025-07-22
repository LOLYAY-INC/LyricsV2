package io.lolyay.lyrics.getters;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.lolyay.lyrics.LyricsNotFoundException;
import io.lolyay.lyrics.Scraper;
import io.lolyay.lyrics.getters.impl.LyricsGetterLiveAble;
import io.lolyay.lyrics.records.Lyrics;
import io.lolyay.lyrics.records.SearchLyrics;
import io.lolyay.lyrics.records.live.LiveData;
import io.lolyay.lyrics.records.live.LiveLyrics;
import io.lolyay.utils.KVPair;
import io.lolyay.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcLibGetter extends LyricsGetterLiveAble {

    private static final Pattern LRC_LINE_PATTERN = Pattern.compile("^\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)$");


    private static final String searchUrlBase = "https://lrclib.net/api/search?q=%s";
    private static final String searchUrlWithArtist = "https://lrclib.net/api/search?track_name=%s&artist_name=%s";

    private String getSearchUrl(String songName) {
        return String.format(searchUrlBase, URLEncoder.encode(songName, StandardCharsets.UTF_8));
    }

    private String getSearchUrl(String songName, String artist) {
        return String.format(searchUrlWithArtist, URLEncoder.encode(songName, StandardCharsets.UTF_8),URLEncoder.encode(artist, StandardCharsets.UTF_8));
    }

    private Map<String, String> getCookies() {
        return new HashMap<>();
    }

    private LyricsApiResponse[] searchLyrics(String songName){
        String searchUrl = getSearchUrl(songName);
        String searchHtml = null;
        try {
            searchHtml = Scraper.getSiteHTML(searchUrl, getCookies());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Gson().fromJson(searchHtml, LyricsApiResponse[].class);
    }

    private LyricsApiResponse[] searchLyrics(String songName, String artist) {
        String searchUrl = getSearchUrl(songName, artist);
        String searchHtml = null;
        try {
            searchHtml = Scraper.getSiteHTML(searchUrl, getCookies());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Gson().fromJson(searchHtml, LyricsApiResponse[].class);
    }

    private LiveLyrics bestMatchLive(LyricsApiResponse[] searchLyrics) {
        LyricsApiResponse bestMatch = searchLyrics[0];
        return new LiveLyrics(new SearchLyrics("lrclib.net/api/get/" + bestMatch.id,bestMatch.trackName,bestMatch.artistName),bestMatch.plainLyrics,getSourceName(),toLiveData(bestMatch));
    }

    private Lyrics bestMatch(LyricsApiResponse[] searchLyrics) {
        LyricsApiResponse bestMatch = searchLyrics[0];
        return new Lyrics(new SearchLyrics("lrclib.net/api/get/" + bestMatch.id,bestMatch.trackName,bestMatch.artistName),bestMatch.plainLyrics,getSourceName());
    }


    private LiveData toLiveData(LyricsApiResponse syncedLyrics) {
        String title = syncedLyrics.trackName;
        String author = syncedLyrics.artistName;
        String source = "lrclib.net/lyrics/" + syncedLyrics.albumName;
        String syncedLyricsString = syncedLyrics.syncedLyrics;
        List<String> allLinesText = new ArrayList<>();
        List<LiveData.TimedLyric> timedLines = new ArrayList<>();

        if (syncedLyricsString == null || syncedLyricsString.isBlank()) {
            // Return an empty object if there's no content to parse
            return new LiveData(title, author, source, allLinesText, timedLines);
        }

        String[] lines = syncedLyricsString.split("\\r?\\n");

        for (String line : lines) {
            Matcher matcher = LRC_LINE_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                // Extract components from the matched line
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                String fractionStr = matcher.group(3);
                int fractions = Integer.parseInt(fractionStr);
                String text = matcher.group(4).trim();

                // Calculate the total timestamp in seconds
                double timestamp = minutes * 60.0 + seconds;
                if (fractionStr.length() == 2) {
                    timestamp += fractions / 100.0; // Hundredths of a second
                } else {
                    timestamp += fractions / 1000.0; // Thousandths of a second
                }

                // Add the lyric text to the list of all lines
                allLinesText.add(text);

                // The globalIndex is the index of the line we just added
                int globalIndex = allLinesText.size() - 1;

                // Create and add the TimedLyric object
                timedLines.add(new LiveData.TimedLyric(timestamp, globalIndex));
            }
            // Lines that don't match the pattern (like metadata tags or empty lines) are ignored.
        }

        return new LiveData(title, author, source, allLinesText, timedLines);
    }

    @Override
    public String getSourceName() {
        return "lrclib.net";
    }

    @Override
    public boolean canGetLyrics(String songName) {
        return true;
    }

    @Override
    public CompletableFuture<Lyrics> getLyrics(String songName) throws LyricsNotFoundException {
        CompletableFuture<Lyrics> future = new CompletableFuture<>();


        Runnable scrapingTask = () -> {
            try {
                Logger.debug("Starting lyrics fetch for song: '{}'".replace("{}", songName));
                future.complete(bestMatch(searchLyrics(songName)));

            } catch (Exception e) {

                Logger.err("Error while fetching lyrics for %s from source %s: %s".formatted(songName, getSourceName(), e.getMessage()));
                future.completeExceptionally(e);
            }
        };


        new Thread(scrapingTask).start();

        return future;
    }

    @Override
    public CompletableFuture<Lyrics> getLyrics(String songName, String artist) throws LyricsNotFoundException {
        CompletableFuture<Lyrics> future = new CompletableFuture<>();


        Runnable scrapingTask = () -> {
            try {
                Logger.debug("Starting lyrics fetch for song: '{}'".replace("{}", songName));
                future.complete(bestMatch(searchLyrics(songName,artist)));

            } catch (Exception e) {

                Logger.err("Error while fetching lyrics for %s from source %s: %s".formatted(songName, getSourceName(), e.getMessage()));
                future.completeExceptionally(e);
            }
        };


        new Thread(scrapingTask).start();

        return future;
    }

    @Override
    public CompletableFuture<LiveLyrics> getLiveLyrics(String songName) {
        CompletableFuture<LiveLyrics> future = new CompletableFuture<>();


        Runnable scrapingTask = () -> {
            try {
                Logger.debug("Starting lyrics fetch for song: '{}'".replace("{}", songName));
                future.complete(bestMatchLive(searchLyrics(songName)));
            } catch (Exception e) {

                Logger.err("Error while fetching lyrics for %s from source %s: %s".formatted(songName, getSourceName(), e.getMessage()));
                future.completeExceptionally(e);
            }
        };


        new Thread(scrapingTask).start();

        return future;
    }

    @Override
    public CompletableFuture<LiveLyrics> getLiveLyrics(String songName, String artist) throws LyricsNotFoundException {
        CompletableFuture<LiveLyrics> future = new CompletableFuture<>();


        Runnable scrapingTask = () -> {
            try {
                Logger.debug("Starting lyrics fetch for song: '{}'".replace("{}", songName));
                future.complete(bestMatchLive(searchLyrics(songName,artist)));
            } catch (Exception e) {

                Logger.err("Error while fetching lyrics for %s from source %s: %s".formatted(songName, getSourceName(), e.getMessage()));
                future.completeExceptionally(e);
            }
        };


        new Thread(scrapingTask).start();

        return future;
    }

    private static class LyricsApiResponse {
        public String id;
        public String trackName;
        public String  artistName;
        public String albumName;
        public double duration;
        public boolean instrumental;
        public String plainLyrics;
        public String syncedLyrics = "No synced lyrics found.";
    }
}
