package igrek.todotree.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmojiTrimTest {

    @Test
    fun testEmotionLessInator() {
        val emotionLessInator = EmotionLessInator()
        assertThat(emotionLessInator.simplify("Emojis✅ 123#$ śółĄŚÓ\t\n⭐.:}\";'!"))
            .isEqualTo("emojis 123#$ śółąśó\t\n.:}\";'!")
    }
}