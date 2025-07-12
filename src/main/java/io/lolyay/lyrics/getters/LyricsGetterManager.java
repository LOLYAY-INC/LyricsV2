package io.lolyay.lyrics.getters;

import io.lolyay.lyrics.getters.impl.LyricsGetter;
import io.lolyay.lyrics.getters.impl.LyricsGetterLiveAble;

import java.util.ArrayList;
import java.util.List;

public abstract class LyricsGetterManager {
    private final static ArrayList<LyricsGetter> lyricsGetters = new ArrayList<>();

    public static void initMusixMatch(String musixMatchCookie) {
        lyricsGetters.add(new MusixMatchGetter(musixMatchCookie));
    }

    public static ArrayList<LyricsGetter> getLyricsGetters() {
        return lyricsGetters;
    }

    public static LyricsGetterLiveAble getLyricsGetterForLive() {
        for (LyricsGetter lgetter : lyricsGetters) {
            if (lgetter.canBeLive()) return (LyricsGetterLiveAble) lgetter;
        }
        return null;
    }

    public static LyricsGetter getLyricsGetterForText(String songName) {
        for (LyricsGetter lgetter : lyricsGetters) {
            if (lgetter.canGetLyrics(songName))
                return lgetter;
        }
        return null;
    }

    public void addLyricsGetter(LyricsGetter getter) {
        lyricsGetters.add(getter);
    }
}
