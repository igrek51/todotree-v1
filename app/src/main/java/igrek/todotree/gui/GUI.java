package igrek.todotree.gui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.List;

import igrek.todotree.R;
import igrek.todotree.gui.edititem.EditItemGUI;
import igrek.todotree.gui.treelist.TreeListView;
import igrek.todotree.logic.controller.AppController;
import igrek.todotree.logic.controller.dispatcher.IEvent;
import igrek.todotree.logic.controller.dispatcher.IEventObserver;
import igrek.todotree.logic.datatree.TreeItem;
import igrek.todotree.logic.events.ExitAppEvent;
import igrek.todotree.logic.events.RotateScreenEvent;
import igrek.todotree.logic.events.ToolbarBackClickedEvent;

public class GUI extends GUIBase implements IEventObserver {

    private ActionBar actionBar;
    private TreeListView itemsListView;
    private EditItemGUI editItemGUI = null;

    public GUI(AppCompatActivity activity) {
        super(activity);

        AppController.registerEventObserver(RotateScreenEvent.class, this);
    }

    @Override
    protected void init() {

        // prohibit from creating the thumbnails
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        activity.setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);
        actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppController.sendEvent(new ToolbarBackClickedEvent());
            }
        });

        //  główna zawartość
        mainContent = (RelativeLayout) activity.findViewById(R.id.mainContent);
    }

    public void showItemsList(final TreeItem currentItem) {
        setOrientationPortrait();

        View itemsListLayout = setMainContentLayout(R.layout.items_list);

        itemsListView = (TreeListView) itemsListLayout.findViewById(R.id.treeItemsList);
        itemsListView.init(activity);
        itemsListView.setItems(currentItem.getChildren());

        updateItemsList(currentItem, null);
    }

    public void showEditItemPanel(final TreeItem item, TreeItem parent) {
        editItemGUI = new EditItemGUI(this, item, parent);
    }

    public void showExitScreen() {

        View exitScreen = setMainContentLayout(R.layout.exit_screen);

        //TODO dalej jest zjebane
        final ViewTreeObserver vto = exitScreen.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppController.sendEvent(new ExitAppEvent());
                    }
                }, 200);
            }
        });
    }

    public void updateItemsList(TreeItem currentItem, List<Integer> selectedPositions) {
        //tytuł gałęzi
        StringBuilder sb = new StringBuilder(currentItem.getContent());
        if (!currentItem.isEmpty()) {
            sb.append(" [");
            sb.append(currentItem.size());
            sb.append("]");
        }
        setTitle(sb.toString());
        //lista elementów
        itemsListView.setItemsAndSelected(currentItem.getChildren(), selectedPositions);
    }

    public void scrollToItem(int position) {
        itemsListView.scrollToItem(position);
    }

    public void scrollToPosition(int y) {
        itemsListView.scrollToPosition(y);
    }

    public void scrollToBottom() {
        itemsListView.scrollToBottom();
    }

    public void hideSoftKeyboard() {
        editItemGUI.hideKeyboards();
    }

    public boolean editItemBackClicked() {
        return editItemGUI.editItemBackClicked();
    }

    public void setTitle(String title) {
        //TODO breadcrumbs przy nazwie aktualnego elementu
        actionBar.setTitle(title);
    }

    public Integer getCurrentScrollPos() {
        return itemsListView.getCurrentScrollPosition();
    }

    public void requestSaveEditedItem() {
        editItemGUI.requestSaveEditedItem();
    }

    private void rotateScreen() {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    private void setOrientationPortrait() {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onEvent(IEvent event) {
        if (event instanceof RotateScreenEvent) {
            rotateScreen();
        }
    }
}
