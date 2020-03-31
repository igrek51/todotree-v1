package igrek.todotree.dagger.base

import android.app.Activity
import igrek.todotree.activity.MainActivity
import igrek.todotree.dagger.DaggerIoc
import igrek.todotree.info.logger.LoggerFactory
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.service.tree.persistence.TreePersistenceService
import org.junit.Before
import org.mockito.Mockito
import javax.inject.Inject

abstract class BaseDaggerTest {

    val logger = LoggerFactory.logger

    @Inject
    protected lateinit var activity: Activity

    @Inject
    protected lateinit var treeManager: TreeManager

    @Inject
    protected lateinit var persistenceService: TreePersistenceService

    @Inject
    protected lateinit var infoService: UserInfoService

    @Inject
    protected lateinit var dbLock: DatabaseLock

    @Before
    fun setUp() {
        val activity = Mockito.mock(MainActivity::class.java)

        val component = DaggerTestComponent.builder()
                .factoryModule(TestModule(activity))
                .build()

        DaggerIoc.factoryComponent = component

        injectThis(component)
    }

    private fun injectThis(component: TestComponent) {
        component.inject(this)
    }
}