package igrek.todotree.dagger

import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.info.logger.LoggerFactory

object DaggerIoc {

    lateinit var factoryComponent: FactoryComponent

    private val logger = LoggerFactory.logger

    private var factoryModule: FactoryModule? = null

    fun init(activity: AppCompatActivity) {
        logger.info("Initializing Dagger IOC container...")
        factoryModule = FactoryModule(activity)
        factoryComponent = DaggerFactoryComponent.builder()
                .factoryModule(factoryModule)
                .build()
    }
}
