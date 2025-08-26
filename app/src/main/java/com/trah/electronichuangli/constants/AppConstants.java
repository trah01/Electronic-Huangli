package com.trah.electronichuangli.constants;

/**
 * 应用常量类
 * 统一管理整个应用中使用的常量
 * 
 * @author trah
 * @version 1.0
 */
public class AppConstants {
    
    // ==================== 传感器相关常量 ====================
    
    /** 指南针滤波系数 - 降低滤波系数提高稳定性 */
    public static final float ALPHA = 0.15f;
    
    /** 指南针精度阈值 - 只有超过此阈值才更新方位 */
    public static final float COMPASS_PRECISION_THRESHOLD = 2f;
    
    /** 传感器更新间隔(毫秒) - 减少抖动 */
    public static final int UPDATE_INTERVAL = 50;
    
    // ==================== SharedPreferences 相关常量 ====================
    
    /** 个人信息存储文件名 */
    public static final String PREFS_PERSONAL_INFO = "PersonalInfo";
    
    /** 生日存储键名 */
    public static final String KEY_BIRTH_DATE = "birth_date";
    
    /** 生辰时间存储键名 */
    public static final String KEY_BIRTH_TIME = "birth_time";
    
    // ==================== 默认值常量 ====================
    
    /** 默认生辰时间 */
    public static final String DEFAULT_BIRTH_TIME = "子时";
    
    /** 未初始化的方位角标志 */
    public static final float UNINITIALIZED_AZIMUTH = -1f;
    
    // ==================== 文件相关常量 ====================
    
    /** 分享图片目录名 */
    public static final String SHARE_IMAGES_DIR = "share_images";
    
    /** 分享图片前缀 */
    public static final String SHARE_IMAGE_PREFIX = "huangli_share_";
    
    /** 图片文件扩展名 */
    public static final String IMAGE_EXTENSION = ".png";
    
    // ==================== 时辰相关常量 ====================
    
    /** 十二时辰名称数组 */
    public static final String[] TWELVE_HOURS = {
        "子时", "丑时", "寅时", "卯时", "辰时", "巳时", 
        "午时", "未时", "申时", "酉时", "戌时", "亥时"
    };
    
    /** 时辰时间范围数组 */
    public static final String[] HOURS_TIME_RANGE = {
        "23-01", "01-03", "03-05", "05-07", "07-09", "09-11",
        "11-13", "13-15", "15-17", "17-19", "19-21", "21-23"
    };
    
    /** 时辰对应时间范围（带括号格式）*/
    public static final String[] HOURS_WITH_TIME = {
        "子时(23-01)", "丑时(01-03)", "寅时(03-05)", "卯时(05-07)", 
        "辰时(07-09)", "巳时(09-11)", "午时(11-13)", "未时(13-15)",
        "申时(15-17)", "酉时(17-19)", "戌时(19-21)", "亥时(21-23)"
    };
    
    // ==================== 方位相关常量 ====================
    
    /** 八个方位对应的度数 */
    public static final class Direction {
        public static final float NORTH = 0f;        // 正北
        public static final float NORTHEAST = 45f;   // 东北
        public static final float EAST = 90f;        // 正东
        public static final float SOUTHEAST = 135f;  // 东南  
        public static final float SOUTH = 180f;      // 正南
        public static final float SOUTHWEST = 225f;  // 西南
        public static final float WEST = 270f;       // 正西
        public static final float NORTHWEST = 315f;  // 西北
    }
    
    // ==================== 颜色相关常量 ====================
    
    /** 界面主要颜色常量 */
    public static final class Colors {
        public static final int PRIMARY_TEXT = 0xFF333333;      // 主要文字色
        public static final int SECONDARY_TEXT = 0xFF666666;    // 次要文字色  
        public static final int LIGHT_GRAY = 0xFF999999;        // 浅灰色
        public static final int BORDER_COLOR = 0xFFE0E0E0;      // 边框色
        public static final int BACKGROUND_GRAY = 0xFFF8F9FA;   // 背景灰色
        
        // 吉利等级颜色
        public static final int GOOD_LUCK = 0xFFE67E22;         // 大吉 - 橙色
        public static final int MEDIUM_LUCK = 0xFF27AE60;       // 中吉 - 绿色  
        public static final int SMALL_LUCK = 0xFF3498DB;        // 小吉 - 蓝色
        
        // 功能颜色
        public static final int SUITABLE_COLOR = 0xFF4CAF50;    // 宜 - 绿色
        public static final int UNSUITABLE_COLOR = 0xFFFF5722; // 忌 - 红色
    }
    
    // ==================== 择日相关常量 ====================
    
    /** 事件类型数组 */
    public static final String[] EVENT_TYPES = {
        "结婚嫁娶", "搬家入宅", "开业开市", "动土修造",
        "出行远行", "签约合同", "祈福祭祀", "安床安门", "理发剪发"
    };
    
    /** 天数范围选项 */
    public static final String[] DAYS_RANGE_OPTIONS = {
        "7天", "15天", "30天", "60天", "90天"
    };
    
    /** 天数范围对应的数值 */
    public static final int[] DAYS_RANGE_VALUES = {7, 15, 30, 60, 90};
    
    /** 默认查找天数范围 */
    public static final int DEFAULT_DAYS_RANGE = 30;
    
    /** 最大结果数量 */
    public static final int MAX_RESULTS = 20;
    
    // ==================== 生肖相关常量 ====================
    
    /** 十二生肖数组 */
    public static final String[] ZODIAC_ANIMALS = {
        "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };
    
    // ==================== SharedPreferences相关常量 ====================
    
    /** 生辰信息SharedPreferences名称 */
    public static final String PREFS_BIRTH_INFO = "BirthInfo";
    
    // ==================== 动画相关常量 ====================
    
    /** 指针旋转动画时长(毫秒) */
    public static final int POINTER_ANIMATION_DURATION = 100;
    
    /** 数据加载延迟时间(毫秒) */
    public static final int DATA_LOADING_DELAY = 5;
    
    /** 下拉刷新延迟时间(毫秒) */
    public static final int REFRESH_DELAY = 500;
    
    // ==================== 私有构造函数，防止实例化 ====================
    private AppConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}