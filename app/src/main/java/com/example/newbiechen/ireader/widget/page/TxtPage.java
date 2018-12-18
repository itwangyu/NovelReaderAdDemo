package com.example.newbiechen.ireader.widget.page;

import java.util.List;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtPage {
    int position;
    String title;
    int titleLines; //当前 lines 中为 title 的行数。
    List<String> lines;

    boolean isCustomView;//标记当前页是否是自填充view
    boolean hasDrawAd;//标记是否已经添加过广告
    String pageType=VALUE_STRING_AD_TYPE;//标记是封面view还是广告view

    public static final String VALUE_STRING_COVER_TYPE="cover",VALUE_STRING_AD_TYPE="ad";
}
