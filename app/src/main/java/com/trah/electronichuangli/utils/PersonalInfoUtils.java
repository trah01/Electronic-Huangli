package com.trah.electronichuangli.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.trah.electronichuangli.constants.AppConstants;
import java.util.Calendar;

/**
 * 个人信息管理工具类
 * 处理用户生辰信息的存储、读取和相关计算
 * 
 * @author trah
 * @version 1.0
 */
public class PersonalInfoUtils {
    
    /**
     * 保存生辰信息到SharedPreferences
     * @param context 上下文
     * @param birthDate 生日日期
     * @param birthTime 生辰时间
     */
    public static void saveBirthInfo(Context context, Calendar birthDate, String birthTime) {
        if (birthDate != null) {
            SharedPreferences prefs = context.getSharedPreferences(
                AppConstants.PREFS_PERSONAL_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(AppConstants.KEY_BIRTH_DATE, birthDate.getTimeInMillis());
            editor.putString(AppConstants.KEY_BIRTH_TIME, birthTime);
            editor.apply();
        }
    }
    
    /**
     * 从SharedPreferences加载生辰信息
     * @param context 上下文
     * @return BirthInfo对象，如果未设置则返回null
     */
    public static BirthInfo loadBirthInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            AppConstants.PREFS_PERSONAL_INFO, Context.MODE_PRIVATE);
        
        long birthDateTime = prefs.getLong(AppConstants.KEY_BIRTH_DATE, -1);
        if (birthDateTime == -1) {
            return null;
        }
        
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTimeInMillis(birthDateTime);
        String birthTime = prefs.getString(AppConstants.KEY_BIRTH_TIME, AppConstants.DEFAULT_BIRTH_TIME);
        
        return new BirthInfo(birthDate, birthTime);
    }
    
    /**
     * 获取时辰名称在数组中的索引
     * @param timeString 时辰字符串（支持带时间范围和不带时间范围的格式）
     * @return 索引值，如果未找到返回0（子时）
     */
    public static int getBirthTimeIndex(String timeString) {
        // 首先尝试匹配完整格式（带时间范围）
        for (int i = 0; i < AppConstants.HOURS_WITH_TIME.length; i++) {
            if (AppConstants.HOURS_WITH_TIME[i].equals(timeString)) {
                return i;
            }
        }
        
        // 然后尝试匹配简单格式（仅时辰名称）
        for (int i = 0; i < AppConstants.TWELVE_HOURS.length; i++) {
            if (AppConstants.TWELVE_HOURS[i].equals(timeString)) {
                return i;
            }
        }
        
        return 0; // 默认返回子时
    }
    
    /**
     * 获取简化的时辰名称（去掉括号内容）
     * @param timeString 原始时辰字符串
     * @return 简化后的时辰名称
     */
    public static String getSimpleTimeName(String timeString) {
        if (timeString.contains("(")) {
            return timeString.substring(0, timeString.indexOf("("));
        }
        return timeString;
    }
    
    /**
     * 生辰信息数据类
     */
    public static class BirthInfo {
        public final Calendar birthDate;
        public final String birthTime;
        
        public BirthInfo(Calendar birthDate, String birthTime) {
            this.birthDate = birthDate;
            this.birthTime = birthTime;
        }
        
        /**
         * 获取出生年份
         */
        public int getBirthYear() {
            return birthDate.get(Calendar.YEAR);
        }
        
        /**
         * 获取出生月份（1-12）
         */
        public int getBirthMonth() {
            return birthDate.get(Calendar.MONTH) + 1;
        }
        
        /**
         * 获取出生日期
         */
        public int getBirthDay() {
            return birthDate.get(Calendar.DAY_OF_MONTH);
        }
        
        /**
         * 获取简化的时辰名称
         */
        public String getSimpleBirthTime() {
            return getSimpleTimeName(birthTime);
        }
    }
}