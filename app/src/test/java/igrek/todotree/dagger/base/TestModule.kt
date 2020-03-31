package igrek.todotree.dagger.base

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import igrek.todotree.dagger.FactoryModule
import igrek.todotree.info.logger.Logger
import igrek.todotree.mock.*
import igrek.todotree.service.clipboard.SystemClipboardManager
import igrek.todotree.service.filesystem.FilesystemService
import igrek.todotree.service.preferences.Preferences
import igrek.todotree.service.resources.UserInfoService
import igrek.todotree.system.filesystem.ExternalCardService
import igrek.todotree.ui.GUI
import org.mockito.Mockito
import org.mockito.Mockito.`when`


class TestModule(activity: AppCompatActivity) : FactoryModule(activity) {

    override fun provideContext(): Context {
        return Mockito.mock(Context::class.java).also {
            `when`(it.getApplicationContext()).thenReturn(it)
        }
    }

    override fun provideLogger(): Logger {
        return LoggerMock()
    }

    override fun provideAppCompatActivity(): AppCompatActivity {
        return Mockito.mock(AppCompatActivity::class.java)
    }

    override fun aUserInfoService(activity: Activity, gui: GUI): UserInfoService = MockedUserInfoService(activity, gui)

    override fun aFilesystemService(context: Context): FilesystemService = MockedFilesystemService(context)

    override fun aExternalCardService(): ExternalCardService = MockedExternalCardService()

    override fun aSystemClipboardManager(activity: Activity): SystemClipboardManager = MockedSystemClipboardManager(activity)

    override fun aGUI(activity: AppCompatActivity): GUI = MockedGUI(activity)

    override fun aPreferences(context: Context): Preferences = MockedPreferences(context)

}