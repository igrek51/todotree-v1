package igrek.todotree.service.filesystem

import igrek.todotree.service.filesystem.FirstFinder.find
import igrek.todotree.service.filesystem.FirstFinder.addRule
import org.assertj.core.api.Java6Assertions
import igrek.todotree.service.filesystem.FirstFinder
import igrek.todotree.service.filesystem.FirstFinder.BooleanCondition
import org.junit.Test

class FirstFinderTest {
    @Test
    fun testEmptyRules() {
        Java6Assertions.assertThat(FirstFinder<Any?>().find()).isNull()
        Java6Assertions.assertThat<Any>(FirstFinder<Any?>().addRule(null).find()).isNull()
    }

    @Test
    fun testDefaultValue() {
        Java6Assertions.assertThat(FirstFinder<String>().addRule("Dupa").find()).isEqualTo("Dupa")
        Java6Assertions.assertThat(FirstFinder<Any?>().addRule(51).find()).isEqualTo(51)
        val nil: Number? = null
        Java6Assertions.assertThat(FirstFinder<Number?>().addRule(nil).find()).isNull()
    }

    @Test
    fun testOneRule() {
        Java6Assertions.assertThat(FirstFinder<String>().addRule({ true }, "dupa").find())
            .isEqualTo("dupa")
        Java6Assertions.assertThat(FirstFinder<String>().addRule({ false }, "dupa").find()).isNull()
        Java6Assertions.assertThat(
            FirstFinder<String>().addRule({ false }, "dupa")
                .addRule("dup")
                .find()
        ).isEqualTo("dup")
    }

    @Test
    fun testManyRules() {
        Java6Assertions.assertThat(
            FirstFinder<String>().addRule({ false }, "dupa1")
                .addRule({ 1 == 2 }, "dupa2")
                .addRule({ 2 * 2 == 4 }, "dupa3")
                .addRule({ true }, "dupa4")
                .addRule("def")
                .find()
        ).isEqualTo("dupa3")
    }

    @Test
    fun testManyRulesOrder() {
        Java6Assertions.assertThat(
            FirstFinder<String>().addRule({ false }, "dupa1")
                .addRule({ false }, "dupa2")
                .addRule({ true }, "dupa3")
                .addRule({ true }, "dupa4")
                .addRule({ true }, "dupa5")
                .find()
        ).isEqualTo("dupa3")
        Java6Assertions.assertThat(
            FirstFinder<String>().addRule({ false }, "dupa1")
                .addRule({ true }, "dupa2")
                .addRule({ false }, "dupa3")
                .addRule({ false }, "dupa4")
                .addRule({ false }, "dupa5")
                .addRule({ false }, "dupa6")
                .addRule({ true }, "dupa7")
                .find()
        ).isEqualTo("dupa2")
    }

    @Test
    fun testProviders() {
        Java6Assertions.assertThat(FirstFinder<String>().addRule { "Dupa" }.find())
            .isEqualTo("Dupa")
        Java6Assertions.assertThat(FirstFinder<String>().addRule({ false }) { "dupa1" }
            .addRule({ 1 == 2 }, "dupa2")
            .addRule({ 2 * 2 == 4 }) { "dupa3" }
            .addRule({ true }, "dupa4")
            .addRule { "def" }
            .find()).isEqualTo("dupa3")
    }
}