package igrek.todotree.activity

import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.access.QuickAddService
import igrek.todotree.system.SystemKeyDispatcher

/*
    Main Activity starter pack
    Workaround for reusing finished activities by Android
 */
class MainActivityData(
    appInitializer: LazyInject<AppInitializer> = appFactory.appInitializer,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    quickAddService: LazyInject<QuickAddService> = appFactory.quickAddService,
    optionSelectDispatcher: LazyInject<OptionSelectDispatcher> = appFactory.optionSelectDispatcher,
    systemKeyDispatcher: LazyInject<SystemKeyDispatcher> = appFactory.systemKeyDispatcher,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) : AppCompatActivity() {
    val appInitializer by LazyExtractor(appInitializer)
    val activityController by LazyExtractor(activityController)
    val quickAddService by LazyExtractor(quickAddService)
    val optionSelectDispatcher by LazyExtractor(optionSelectDispatcher)
    val systemKeyDispatcher by LazyExtractor(systemKeyDispatcher)
    val activityResultDispatcher by LazyExtractor(activityResultDispatcher)
}
