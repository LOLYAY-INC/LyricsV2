package io.lolyay.lyrics.records.live;


import io.lolyay.lyrics.records.SearchLyrics;

public record LiveLyrics(SearchLyrics query, String content, String source, LiveData liveData) {
}
