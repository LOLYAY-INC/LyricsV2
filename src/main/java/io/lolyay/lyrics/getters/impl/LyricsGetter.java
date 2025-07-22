package io.lolyay.lyrics.getters.impl;



import io.lolyay.lyrics.LyricsNotFoundException;
import io.lolyay.lyrics.records.Lyrics;

import java.util.concurrent.CompletableFuture;

public abstract class LyricsGetter {
    public abstract String getSourceName();

    public abstract boolean canGetLyrics(String songName);

    public abstract CompletableFuture<Lyrics> getLyrics(String query) throws LyricsNotFoundException;

    public abstract CompletableFuture<Lyrics> getLyrics(String songName, String artist) throws LyricsNotFoundException;

    public boolean canBeLive() {
        return false;
    }
}
