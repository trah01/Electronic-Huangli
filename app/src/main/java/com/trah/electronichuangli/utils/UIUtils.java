package com.trah.electronichuangli.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import com.trah.electronichuangli.constants.AppConstants;

/**
 * UI工具类
 * 提供创建卡片、设置样式等UI相关的工具方法
 * 
 * @author trah
 * @version 1.0
 */
public class UIUtils {
    
    /**
     * 创建简洁的信息卡片
     * @param context 上下文
     * @param title 标题
     * @param content 内容
     * @param titleColor 标题颜色
     * @param backgroundColor 背景颜色
     * @return 创建的LinearLayout卡片
     */
    public static LinearLayout createSimpleCard(Context context, String title, String content, 
                                              int titleColor, int backgroundColor) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 15, 20, 15);
        card.setBackgroundColor(backgroundColor);
        
        // 创建简洁的边框
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(backgroundColor);
        drawable.setStroke(1, AppConstants.Colors.BORDER_COLOR);
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(13);
        titleView.setTextColor(titleColor);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(context);
        contentView.setText(content);
        contentView.setTextSize(15);
        contentView.setTextColor(AppConstants.Colors.PRIMARY_TEXT);
        contentView.setPadding(0, 5, 0, 0);
        card.addView(contentView);
        
        // 设置布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);
        
        return card;
    }
    
    /**
     * 创建宜忌样式的卡片
     * @param context 上下文
     * @param title 标题
     * @param content 内容
     * @param titleColor 标题颜色
     * @param backgroundColor 背景颜色
     * @return 创建的LinearLayout卡片
     */
    public static LinearLayout createYiJiCard(Context context, String title, String content,
                                            int titleColor, int backgroundColor) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        
        // 创建简洁的背景
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(backgroundColor);
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(14);
        titleView.setTextColor(titleColor);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(context);
        contentView.setText(content);
        contentView.setTextSize(13);
        contentView.setTextColor(Color.parseColor("#34495E"));
        contentView.setGravity(Gravity.CENTER);
        contentView.setPadding(0, 8, 0, 0);
        contentView.setLineSpacing(6, 1.0f);
        card.addView(contentView);
        
        // 设置布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        card.setLayoutParams(params);
        
        return card;
    }
    
    /**
     * 创建小卡片（用于三神方位等）
     * @param context 上下文
     * @param title 标题
     * @param content 内容
     * @param titleColor 标题颜色
     * @return 创建的LinearLayout小卡片
     */
    public static LinearLayout createSmallCard(Context context, String title, String content, 
                                             int titleColor) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(12, 15, 12, 15);
        
        // 创建简洁的背景
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(Color.parseColor("#FAFBFC"));
        drawable.setStroke(1, titleColor);
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(11);
        titleView.setTextColor(titleColor);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(context);
        contentView.setText(content);
        contentView.setTextSize(13);
        contentView.setTextColor(AppConstants.Colors.PRIMARY_TEXT);
        contentView.setPadding(0, 3, 0, 0);
        card.addView(contentView);
        
        // 设置布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        card.setLayoutParams(params);
        
        return card;
    }
    
    /**
     * 根据吉利等级获取对应的颜色
     * @param quality 吉利等级（"大吉"、"中吉"、"小吉"）
     * @return 对应的颜色值
     */
    public static int getQualityColor(String quality) {
        if ("大吉".equals(quality)) {
            return AppConstants.Colors.GOOD_LUCK;
        } else if ("中吉".equals(quality)) {
            return AppConstants.Colors.MEDIUM_LUCK;
        } else {
            return AppConstants.Colors.SMALL_LUCK;
        }
    }
    
    /**
     * 设置TextView的样式
     * @param textView 要设置的TextView
     * @param textSize 文字大小
     * @param textColor 文字颜色
     * @param isBold 是否加粗
     */
    public static void setTextStyle(TextView textView, float textSize, int textColor, boolean isBold) {
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        if (isBold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}