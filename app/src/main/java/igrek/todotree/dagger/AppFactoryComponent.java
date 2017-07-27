package igrek.todotree.dagger;


import javax.inject.Singleton;

import dagger.Component;
import igrek.todotree.filesystem.FilesystemService;
import igrek.todotree.gui.GUI;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.logic.ClipboardManager;
import igrek.todotree.logic.LogicActionController;
import igrek.todotree.logic.app.App;
import igrek.todotree.logic.backup.BackupManager;
import igrek.todotree.logic.datatree.TreeManager;
import igrek.todotree.preferences.Preferences;
import igrek.todotree.resources.UserInfoService;

@Singleton
@Component(modules = {AppFactoryModule.class})
public interface AppFactoryComponent {
	
	void inject(IDaggerInjectionTest there);
	
	void inject(App there);
	
	void inject(TreeListView there);
	
	void inject(EditItemGUI there);
	
	void inject(GUI there);
	
	void inject(FilesystemService there);
	
	void inject(TreeManager there);
	
	void inject(Preferences there);
	
	void inject(BackupManager there);
	
	void inject(UserInfoService there);
	
	void inject(LogicActionController there);
	
	void inject(ClipboardManager there);
	
}