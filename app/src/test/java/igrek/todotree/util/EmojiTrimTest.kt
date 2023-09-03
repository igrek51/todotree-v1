package igrek.todotree.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmojiTrimTest {

    @Test
    fun testEmotionLessInator() {
        val emotionLessInator = EmotionLessInator()
        assertThat(emotionLessInator.simplify("Emojis✅ 123!@#\$%^&*()_+ śółĄŚÓ\t\n⭐.:}\";'!"))
            .isEqualTo("emojis 123!@#\$%^&*()_+ śółąśó\t\n.:}\";'!")
        assertThat(emotionLessInator.simplify("\uD83D\uDE00"))
            .isEqualTo("")
    }
}