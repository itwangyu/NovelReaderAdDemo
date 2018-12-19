package com.example.newbiechen.ireader.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by WangYu on 2018/11/20.
 */
public class UtilsView {

    public static void removeParent(View view) {
        try {
            if (view == null) {
                return;
            }
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent == null) {
                return;
            }
            parent.removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
