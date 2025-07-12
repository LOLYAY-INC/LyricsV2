package io.lolyay.lyrics.records;

public record SearchLyrics(String url, String title, String author) {
    @Override
    public String toString() {
        return "SearchLyrics{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
