package com.android.theguardian.tagdragview;

import android.content.Context;

import com.android.theguardian.Utils;
import com.android.theguardian.tagdragview.bean.SimpleTitleTip;

import java.util.ArrayList;
import java.util.List;

public class TipDataModel {

    public static List<SimpleTitleTip> getDragTips(Context context) {
        List<String> dragTips = Utils.getSelectedTags(context);
        List<SimpleTitleTip> result = new ArrayList<>();
        for (int i = 0; i < dragTips.size(); i++) {
            String temp = dragTips.get(i);
            SimpleTitleTip tip = new SimpleTitleTip();
            tip.setTip(temp);
            tip.setId(i);
            result.add(tip);
        }
        return result;
    }

    public static List<SimpleTitleTip> getAddTips(Context context) {
        List<String> addTips = Utils.getUnselectedTags(context);
        List<String> dragTips = Utils.getSelectedTags(context);
        List<SimpleTitleTip> result = new ArrayList<>();
        for (int i = 0; i < addTips.size(); i++) {
            String temp = addTips.get(i);
            SimpleTitleTip tip = new SimpleTitleTip();
            tip.setTip(temp);
            tip.setId(i + dragTips.size());
            result.add(tip);
        }
        return result;
    }
}
