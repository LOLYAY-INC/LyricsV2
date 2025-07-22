package io.lolyay.lyrics.getters.impl;



import io.lolyay.lyrics.LyricsNotFoundException;
import io.lolyay.lyrics.records.live.LiveLyrics;

import java.util.concurrent.CompletableFuture;

public abstract class LyricsGetterLiveAble extends LyricsGetter {
    @Override
    public boolean canBeLive() {
        return true;
    }

    public abstract CompletableFuture<LiveLyrics> getLiveLyrics(String songName) throws LyricsNotFoundException;
    public abstract CompletableFuture<LiveLyrics> getLiveLyrics(String songName, String artist) throws LyricsNotFoundException;

}
