package igrek.todotree.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

public abstract class BaseGUI {

    protected AppCompatActivity activity;

    protected InputMethodManager imm;

    protected RelativeLayout mainContent;
    
    public BaseGUI(AppCompatActivity activity) {
        this.activity = activity;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public RelativeLayout getMainContent() {
        return mainContent;
    }

    public View setMainContentLayout(int layoutResource) {
        mainContent.removeAllViews();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutResource, null);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainContent.addView(layout);
        return layout;
    }


    public void hideSoftKeyboard(View window) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View window) {
        if (imm != null) {
            imm.showSoftInput(window, 0);
        }
    }

}
