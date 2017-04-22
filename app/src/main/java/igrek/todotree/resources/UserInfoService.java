package igrek.todotree.resources;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.HashMap;

import igrek.todotree.logger.Logs;
import igrek.todotree.logic.controller.AppController;
import igrek.todotree.logic.controller.services.IService;

public class UserInfoService implements IService {
    
    private Activity activity;
    private View mainView = null;
    
    private HashMap<View, Snackbar> infobars = new HashMap<>();
    
    public UserInfoService(Activity activity) {
        this.activity = activity;
        AppController.registerService(this);
    }
    
    public String resString(int resourceId) {
        return activity.getResources().getString(resourceId);
    }
    
    public void setMainView(View mainView) {
        this.mainView = mainView;
        infobars.clear();
    }
    
    /**
     * @param info       tekst do wyświetlenia lub zmiany
     * @param view       widok, na którym ma zostać wyświetlony tekst
     * @param actionName tekst przycisku akcji (jeśli null - brak przycisku akcji)
     * @param action     akcja kliknięcia przycisku (jeśli null - schowanie wyświetlanego tekstu)
     */
    public void showActionInfo(String info, View view, String actionName, InfoBarClickAction action, Integer color) {
        
        if (view == null) {
            view = mainView;
        }
        
        Snackbar snackbar = infobars.get(view);
        if (snackbar == null || !snackbar.isShown()) { //nowy
            snackbar = Snackbar.make(view, info, Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(Color.WHITE);
        } else { //widoczny - użyty kolejny raz
            snackbar.setText(info);
        }
        
        if (actionName != null) {
            if (action == null) {
                final Snackbar finalSnackbar = snackbar;
                action = new InfoBarClickAction() {
                    @Override
                    public void onClick() {
                        finalSnackbar.dismiss();
                    }
                };
            }
            
            final InfoBarClickAction finalAction = action;
            snackbar.setAction(actionName, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalAction.onClick();
                }
            });
            if (color != null) {
                snackbar.setActionTextColor(color);
            }
        }
        
        snackbar.show();
        infobars.put(view, snackbar);
        Logs.info(info);
    }
    
    public void showActionInfo(int resourceId, View view, String actionName, InfoBarClickAction action, Integer color) {
        showActionInfo(resString(resourceId), view, actionName, action, color);
    }
    
    public void hideInfo(View view) {
        final Snackbar snackbar = infobars.get(view);
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
