package io.lolyay.lyrics.records.live;

import java.util.List;

public record LiveData(
        String title,
        String author,
        String source,
        List<String> allLinesText,
        List<TimedLyric> timedLines
) {
    public record TimedLyric(double timestamp, int globalIndex) {
    }
}