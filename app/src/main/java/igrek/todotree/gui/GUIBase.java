package igrek.todotree.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import igrek.todotree.logic.datatree.TreeItem;

public class GUIBase {

    AppCompatActivity activity;
    GUIListener guiListener;

    InputMethodManager imm;

    RelativeLayout mainContent;

    public GUIBase(AppCompatActivity activity, GUIListener guiListener) {
        this.activity = activity;
        this.guiListener = guiListener;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        init();
    }

    protected void init() {

    }

    public RelativeLayout getMainContent() {
        return mainContent;
    }

    protected View setMainContentLayout(int layoutResource) {
        mainContent.removeAllViews();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutResource, null);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainContent.addView(layout);
        return layout;
    }


    protected String getTreeItemText(TreeItem item) {
        StringBuilder sb = new StringBuilder(item.getContent());
        sb.append(" [");
        sb.append(item.size());
        sb.append("]:");
        return sb.toString();
    }

    protected void hideSoftKeyboard(View window) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
        }
    }

    protected void showSoftKeyboard(View window) {
        if (imm != null) {
            imm.showSoftInput(window, 0);
        }
    }
}
