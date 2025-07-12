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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MusixMatchGetter extends LyricsGetterLiveAble {
    private final String userCookie;

    public MusixMatchGetter(String userCookie) {
        this.userCookie = userCookie;
    }

    private static final String searchUrlBase = "https://www.musixmatch.com/search?query=%s";

    private String getSearchUrl(String songName) {
        return String.format(searchUrlBase, URLEncoder.encode(songName, StandardCharsets.UTF_8));
    }

    private Map<String, String> getCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("musixmatchUserToken", userCookie);
        return cookies;
    }

    private SearchLyrics processSearchResultsDom(Document document) {
        String bestResultSelector = "div:contains(Best result) + div a[href^='/lyrics/']";
        Element bestResultLink = document.selectFirst(bestResultSelector);

        if (bestResultLink == null) {
            Logger.warn("Could not find 'Best result' section, falling back to first track.");
            bestResultLink = document.selectFirst("a[href^='/lyrics/']");
        }

        if (bestResultLink != null) {

            String url = bestResultLink.attr("abs:href");

            Elements infoElements = bestResultLink.select("div[dir=auto]");

            String title = null;
            String author = null;

            if (infoElements.size() >= 2) {
                title = infoElements.get(0).text().trim();
                author = infoElements.get(1).text().trim();
            }

            if (title != null) {
                return new SearchLyrics(url, title, author);
            } else {
                Logger.err("A lyric link was found, but title/author elements inside it were missing.");
                return null;
            }
        } else {
            Logger.err("No lyric links found on the page.");
            return null;
        }
    }

    private KVPair<String, String> parseLyricsDom(Document document) throws Exception {
        Element scriptElement = document.getElementById("__NEXT_DATA__");

        if (scriptElement == null) {
            Logger.err("Could not find the __NEXT_DATA__ script tag. The page structure may have changed.");
            throw new Exception("Lyrics data script not found.");
        }

        String jsonData = scriptElement.html();
        JSONObject rootJson = new JSONObject(jsonData);


        try {
            String lyricsBody = rootJson.getJSONObject("props")
                    .getJSONObject("pageProps")
                    .getJSONObject("data")
                    .getJSONObject("trackInfo")
                    .getJSONObject("data")
                    .getJSONObject("lyrics")
                    .getString("body");


            return new KVPair<>(cleanLyrics(lyricsBody), jsonData);

        } catch (org.json.JSONException e) {
            Logger.err("Failed to parse lyrics from JSON. The JSON structure might have changed. " + e.getMessage());
            throw new Exception("Could not find 'body' in the lyrics JSON object.");
        }
    }


    private String cleanLyrics(String lyricsText) {
        String cleaned = lyricsText.replaceAll("(?s)Writer\\(s\\):.*", "");

        return cleaned
                .replaceAll("(?i)\\[?(verse|chorus|hook|intro|outro|bridge|pre-chorus|refrain|post-chorus|interlude|breakdown|solo|instrumental)\\]?\\s*[:\\s]*[0-9]*", "")
                .replaceFirst("(?i)^Lyrics of .* by .*\r?\n?", "")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    @Override
    public String getSourceName() {
        return "MusixMatch";
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


                String searchUrl = getSearchUrl(songName);
                String searchHtml = Scraper.getSiteHTML(searchUrl, getCookies());
                Document searchDocument = Jsoup.parse(searchHtml, searchUrl);
                SearchLyrics searchResult = processSearchResultsDom(searchDocument);


                if (searchResult == null || searchResult.url() == null || searchResult.url().isEmpty()) {
                    Logger.debug("No valid search result found for '{}'.".replace("{}", songName));

                    throw new LyricsNotFoundException(searchResult == null ? new SearchLyrics(null, songName, null) : searchResult, getSourceName());
                }

                Logger.debug("Found potential match for '{}': {}".replace("{}", songName).replace("{}", searchResult.toString()));


                String lyricsUrl = searchResult.url();
                String lyricsHtml = Scraper.getSiteHTML(lyricsUrl, getCookies());
                Document lyricsDocument = Jsoup.parse(lyricsHtml, lyricsUrl);
                KVPair<String, String> lyricsTextAndLivePart = parseLyricsDom(lyricsDocument);


                Logger.debug("Successfully parsed lyrics for %s from source %s.".formatted(songName, getSourceName()));
                Lyrics finalLyrics = new Lyrics(searchResult, lyricsTextAndLivePart.getKey(), getSourceName());
                future.complete(finalLyrics);

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


                String searchUrl = getSearchUrl(songName);
                String searchHtml = Scraper.getSiteHTML(searchUrl, getCookies());
                Document searchDocument = Jsoup.parse(searchHtml, searchUrl);
                SearchLyrics searchResult = processSearchResultsDom(searchDocument);


                if (searchResult == null || searchResult.url() == null || searchResult.url().isEmpty()) {
                    Logger.debug("No valid search result found for '{}'.".replace("{}", songName));

                    throw new LyricsNotFoundException(searchResult == null ? new SearchLyrics(null, songName, null) : searchResult, getSourceName());
                }

                Logger.debug("Found potential match for '{}': {}".replace("{}", songName).replace("{}", searchResult.toString()));


                String lyricsUrl = searchResult.url();
                String lyricsHtml = Scraper.getSiteHTML(lyricsUrl, getCookies());
                Document lyricsDocument = Jsoup.parse(lyricsHtml, lyricsUrl);
                KVPair<String, String> lyricsTextAndLivePart = parseLyricsDom(lyricsDocument);


                Logger.debug("Successfully parsed lyrics for %s from source %s.".formatted(songName, getSourceName()));
                LiveLyrics finalLyrics = new LiveLyrics(searchResult, lyricsTextAndLivePart.getKey(), getSourceName(), parse(lyricsTextAndLivePart.getValue()));
                future.complete(finalLyrics);

            } catch (Exception e) {

                Logger.err("Error while fetching lyrics for %s from source %s: %s".formatted(songName, getSourceName(), e.getMessage()));
                future.completeExceptionally(e);
            }
        };


      new Thread(scrapingTask).start();

        return future;
    }


    // SYNCED LYRICS

    public LiveData parse(String fullJsonString) throws JSONException, JsonSyntaxException {
        JSONObject root = new JSONObject(fullJsonString);
        JSONObject pageProps = root.getJSONObject("props").getJSONObject("pageProps");
        JSONObject trackData = pageProps.getJSONObject("data").getJSONObject("trackInfo").getJSONObject("data");
        JSONArray trackStructureList = trackData.getJSONArray("trackStructureList");

        String title = trackData.getJSONObject("track").getString("name");
        String author = trackData.getJSONObject("track").getString("artistName");
        String source = "musixmatch.com/lyrics/" + trackData.getJSONObject("track").getString("vanityId");

        Gson gson = new Gson();
        List<LyricData.LyricSection> sections = gson.fromJson(trackStructureList.toString(), new TypeToken<List<LyricData.LyricSection>>() {
        }.getType());

        List<String> allLinesText = new ArrayList<>();
        List<LiveData.TimedLyric> timedLines = new ArrayList<>();
        flattenLyrics(sections, allLinesText, timedLines);

        return new LiveData(title, author, source, allLinesText, timedLines);
    }

    private void flattenLyrics(List<LyricData.LyricSection> sections, List<String> allLinesText, List<LiveData.TimedLyric> timedLines) {
        AtomicInteger globalIndex = new AtomicInteger(0);
        for (LyricData.LyricSection section : sections) {
            if (section.title() != null && !section.title().isEmpty()) {
                allLinesText.add("[" + section.title() + "]");
                globalIndex.getAndIncrement();
            }
            if (section.lines() != null) {
                for (LyricData.LyricLine line : section.lines()) {
                    String text = (line.text() != null && !line.text().isEmpty()) ? line.text() : "♪";
                    allLinesText.add(text);
                    if (line.time() != null) {
                        timedLines.add(new LiveData.TimedLyric(line.time().total(), globalIndex.get()));
                    }
                    globalIndex.getAndIncrement();
                }
            }
            allLinesText.add("");
            globalIndex.getAndIncrement();
        }
    }

    private record LyricData() {
        record LyricSection(String title, List<LyricLine> lines) {
        }

        record LyricLine(String text, LyricTime time) {
        }

        record LyricTime(double total) {
        }
    }
}
