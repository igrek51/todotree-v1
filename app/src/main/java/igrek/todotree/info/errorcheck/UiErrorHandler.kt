package igrek.todotree.info.errorcheck

import igrek.todotree.R
import igrek.todotree.info.UiInfoService
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory


class UiErrorHandler(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
        LoggerFactory.logger.error(t)
        val err: String = when {
            t.message != null -> t.message
            else -> t::class.simpleName
        }.orEmpty()

        uiInfoService.showInfoAction(contextResId, err, indefinite = true, actionResId = R.string.error_details) {
            showDetails(t, contextResId)
        }
    }

    private fun showDetails(t: Throwable, contextResId: Int) {
        val errorMessage = uiInfoService.resString(contextResId, t.message.orEmpty())
        val message = "${errorMessage}\nType: ${t::class.simpleName}"
        uiInfoService.dialog(titleResId = R.string.error_occurred, message = message)
    }

    companion object {
        fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
            UiErrorHandler().handleError(t, contextResId)
        }
    }

}
