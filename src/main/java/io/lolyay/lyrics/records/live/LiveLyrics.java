package io.lolyay.lyrics.records.live;


import com.google.gson.annotations.Expose;
import io.lolyay.lyrics.records.Lyrics;
import io.lolyay.lyrics.records.SearchLyrics;

public record LiveLyrics(@Expose SearchLyrics query,@Expose String content,@Expose String source,@Expose LiveData liveData) {
}
