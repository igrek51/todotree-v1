package igrek.todotree.logic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import igrek.todotree.R;
import igrek.todotree.logic.tree.TreeItem;
import igrek.todotree.logic.tree.TreeItemListener;
import igrek.todotree.logic.tree.TreeManager;
import igrek.todotree.logic.tree.exceptions.NoSuperItemException;
import igrek.todotree.view.treelist.TreeItemAdapter;

public class App extends BaseApp implements TreeItemListener {

    ViewSwitcher mainContentSwicther;
    View items_list_layout;
    View edit_item_content_layout;

    ListView listview;
    TextView list_title;
    TreeItemAdapter adapter;

    TreeManager treeManager;

    public App(AppCompatActivity activity) {
        super(activity);
        preferences.preferencesLoad();

        treeManager = new TreeManager();

        TreeItem item1 = treeManager.getCurrentItem().add("Dupa");
        item1.add("d");
        item1.add("u");
        item1.add("pa");
        treeManager.getCurrentItem().add("Dupa, dupa");
        TreeItem item3 = treeManager.getCurrentItem().add("Dupa i ****");
        for (int i = 0; i < 30; i++) {
            item3.add("numerek " + i);
        }


        //  ZBUDOWANIE LAYOUTU
        //TODO: layout builder

        activity.setContentView(R.layout.activity_main);

        //toolbar
        Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
        activity.setSupportActionBar(toolbar1);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });

        //  główna zawartość

        mainContentSwicther = (ViewSwitcher) activity.findViewById(R.id.mainContentSwicther);

        Animation slide_in_left = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left);
        Animation slide_out_right = AnimationUtils.loadAnimation(activity, android.R.anim.slide_out_right);
        mainContentSwicther.setInAnimation(slide_in_left);
        mainContentSwicther.setOutAnimation(slide_out_right);

        items_list_layout = activity.findViewById(R.id.items_list);

        edit_item_content_layout = activity.findViewById(R.id.edit_item_content);

        showItemsList();
    }

    public void showItemsList(){


        if (mainContentSwicther.getCurrentView() == edit_item_content_layout){
            mainContentSwicther.showPrevious();
        }


        //przycisk dodawania
        FloatingActionButton fab = (FloatingActionButton) items_list_layout.findViewById(R.id.fab);
        //TODO: sparametryzować kolorki i wrzucić do Config albo values
        fab.setBackgroundTintList(ColorStateList.valueOf(0xff0080a0));
        fab.setColorFilter(0xff000060);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfo("Replace with your own action", view);
            }
        });

        list_title = (TextView) items_list_layout.findViewById(R.id.list_title);

        listview = (ListView) items_list_layout.findViewById(R.id.listview1);

        adapter = new TreeItemAdapter(activity, treeManager.getCurrentItem().getChildren(), this);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                //                TreeItem item = (TreeItem) parent.getItemAtPosition(position);
                treeManager.goInto(position);
                updateListModel();
            }
        });

        mainContentSwicther.showNext();

        updateListModel();
    }

    public void updateListModel() {
        //tytuł gałęzi
        list_title.setText(treeManager.getCurrentItem().getContent() + " [" + treeManager.getCurrentItem().size() + "]:");
        //lista elementów
        adapter.setDataSource(treeManager.getCurrentItem().getChildren());
    }


    @Override
    public boolean optionsSelect(int id) {
        if (id == R.id.action_settings) {

            return true;
        }
        return false;
    }

    @Override
    public boolean keycodeBack() {
        goUp();
        return true;
    }

    public void goUp() {
        try {
            treeManager.goUp();
            updateListModel();
        } catch (NoSuperItemException e) {
            quit();
        }
    }

    public void showInfo(String info) {
        showInfo(info, mainContentSwicther);
    }

    public void showEditItemPanel(final TreeItem item) {

        if (mainContentSwicther.getCurrentView() == items_list_layout){
            mainContentSwicther.showNext();
        }

        treeManager.setEditItem(item);

        TextView tv_item_title = (TextView) edit_item_content_layout.findViewById(R.id.tv_item_title);
        final EditText et_edit_item = (EditText) edit_item_content_layout.findViewById(R.id.et_edit_item);
        Button button_save_item = (Button) edit_item_content_layout.findViewById(R.id.button_save_item);

        tv_item_title.setText(treeManager.getCurrentItem().getContent() + " [" + treeManager.getCurrentItem().size() + "]:");

        et_edit_item.setText(item.getContent());

        button_save_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem(item, et_edit_item.getText().toString());
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_edit_item.getWindowToken(), 0);
            }
        });
    }

    public void saveItem(TreeItem editItem, String content){
        editItem.setContent(content);

        showItemsList();

        showInfo("Zapisano element");
    }

    @Override
    public void onTreeItemClicked(int position, TreeItem item) {
        showEditItemPanel(item);
    }
}
