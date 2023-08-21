package igrek.todotree.info.errorcheck

import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.appFactory

class LocalizedError(
    val messageRes: Int,
) : RuntimeException() {
    private val uiInfoService by LazyExtractor(appFactory.uiInfoService)

    override val message: String
        get() {
            return uiInfoService.resString(messageRes)
        }
}