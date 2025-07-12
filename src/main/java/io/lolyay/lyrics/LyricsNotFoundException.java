package io.lolyay.lyrics;


import io.lolyay.lyrics.records.SearchLyrics;

public class LyricsNotFoundException extends Exception {
    public LyricsNotFoundException(SearchLyrics message,
                                   String source) {
        super(String.format("Lyrics for %s by %s not found in %s", message.title(), message.author(), source));
    }
}
