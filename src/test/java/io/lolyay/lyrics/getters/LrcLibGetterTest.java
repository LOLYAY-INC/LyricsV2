package io.lolyay.lyrics.getters;

import io.lolyay.lyrics.getters.LrcLibGetter;
import io.lolyay.lyrics.records.Lyrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*; // Import all assertions


public class LrcLibGetterTest {
    @Test
    @DisplayName("Should get lyrics for a valid query without throwing an exception")
    void shouldGetLyricsForValidQuery() {
        // Arrange
        String query = "never gonna give you up";
        LrcLibGetter getter = new LrcLibGetter();

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