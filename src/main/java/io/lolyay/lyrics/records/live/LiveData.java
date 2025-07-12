package io.lolyay.lyrics.records.live;

import com.google.gson.annotations.Expose;

import java.util.List;

public record LiveData(
        @Expose
        String title,
        @Expose
        String author,
        @Expose
        String source,
        @Expose
        List<String> allLinesText,
        @Expose
        List<TimedLyric> timedLines
) {
    public record TimedLyric(@Expose double timestamp,@Expose int globalIndex) {
    }
}