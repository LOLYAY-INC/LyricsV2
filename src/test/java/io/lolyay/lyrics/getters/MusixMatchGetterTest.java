package io.lolyay.lyrics.getters;

import io.lolyay.lyrics.records.Lyrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MusixMatchGetterTest {
    @Test
    @DisplayName("Should get lyrics for a valid query without throwing an exception")
    void shouldGetLyricsForValidQuery() {
        // Arrange
        String query = "never gonna give you up";
        MusixMatchGetter getter = new MusixMatchGetter("%7B%22tokens%22%3A%7B%22mxm-account-v1.0%22%3A%222507f7c8e61721f5ee027b1e9f24bd3f3817ca0a37115c8e8f34%22%2C%22mxm-com-v1.0%22%3A%222507c424ab1e2c4b1237d4837b2246f97f6679703fad0791181a%22%2C%22web-desktop-app-v1.0%22%3A%222507e12dd9c9a77c71a34ea6fa76e878d40f66cbc03cf7076982%22%2C%22musixmatch-podcasts-v2.0%22%3A%222507683ca4c0e281b3c06b7d4c497330306e072daa387a02daf6%22%2C%22mxm-pro-web-v1.0%22%3A%222507ea8d2e0afd4ea66926864db6a52a5ae0fcbd49c06690ef6d%22%2C%22musixmatch-publishers-v2.0%22%3A%2225073635820ad1415affe3977262dbbda2a67ee1dd008fa09e96%22%7D%2C%22version%22%3A1%7D");

        // Act & Assert
        // assertDoesNotThrow will execute the code in the lambda.
        // The test passes if no exception is thrown.
        // The test fails if any exception is thrown.
        Lyrics result = assertDoesNotThrow(() -> getter.getLyrics(query).join(),
                "Getting lyrics for a known song should not fail.");

        // Add actual assertions to verify the result!
        assertNotNull(result, "The returned lyrics object should not be null.");
        assertNotNull(result.content(), "The lyrics content string should not be null.");
        assertTrue(result.content().toLowerCase().contains("never gonna give you up"),
                "The lyrics should contain the song title.");
    }
}