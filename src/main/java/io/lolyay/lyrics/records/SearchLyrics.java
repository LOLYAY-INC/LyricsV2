package io.lolyay.lyrics.records;

import com.google.gson.annotations.Expose;

public record SearchLyrics(@Expose String url,@Expose String title,@Expose String author) {
    @Override
    public String toString() {
        return "SearchLyrics{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
