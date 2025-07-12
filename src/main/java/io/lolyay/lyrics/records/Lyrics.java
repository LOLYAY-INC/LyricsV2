package io.lolyay.lyrics.records;

import com.google.gson.annotations.Expose;

public record Lyrics(@Expose SearchLyrics query,@Expose String content,@Expose String source) {
}
