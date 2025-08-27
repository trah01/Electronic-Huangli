package com.trah.electronichuangli.utils;

/**
 * 运势数据模型类
 * 封装个人运势计算结果的各种数据
 * 
 * @author trah
 * @version 1.0
 */
public class FortuneData {
    
    // ==================== 运势评分 ====================
    private int wealthScore;      // 财运评分 (0-5星)
    private int careerScore;      // 事业运评分 (0-5星)
    private int loveScore;        // 感情运评分 (0-5星)
    private int healthScore;      // 健康运评分 (0-5星)
    
    // ==================== 运势描述 ====================
    private String wealthDesc;    // 财运描述
    private String careerDesc;    // 事业运描述
    private String loveDesc;      // 感情运描述
    private String healthDesc;    // 健康运描述
    
    // ==================== 开运建议 ====================
    private String[] luckyColors;    // 幸运颜色
    private int[] luckyNumbers;      // 幸运数字
    private String luckyTime;        // 吉时
    private String luckyDirection;   // 贵人方位
    
    // ==================== 八字信息 ====================
    private String baziInfo;         // 八字信息
    private String wuxingBalance;    // 五行平衡
    private String yongshen;         // 用神
    private String jishen;           // 忌神
    
    // ==================== 今日特殊提醒 ====================
    private String todayWarning;     // 今日特别注意事项
    private String todayAdvice;      // 今日建议
    
    /**
     * 默认构造函数
     */
    public FortuneData() {
        this.luckyColors = new String[]{"红色", "金色"};
        this.luckyNumbers = new int[]{3, 8};
        this.luckyTime = "子时";
        this.luckyDirection = "东南方";
        this.todayWarning = "";
        this.todayAdvice = "";
    }
    
    /**
     * 完整构造函数
     */
    public FortuneData(int wealthScore, int careerScore, int loveScore, int healthScore,
                      String wealthDesc, String careerDesc, String loveDesc, String healthDesc,
                      String[] luckyColors, int[] luckyNumbers, String luckyTime, String luckyDirection) {
        this.wealthScore = wealthScore;
        this.careerScore = careerScore;
        this.loveScore = loveScore;
        this.healthScore = healthScore;
        this.wealthDesc = wealthDesc;
        this.careerDesc = careerDesc;
        this.loveDesc = loveDesc;
        this.healthDesc = healthDesc;
        this.luckyColors = luckyColors;
        this.luckyNumbers = luckyNumbers;
        this.luckyTime = luckyTime;
        this.luckyDirection = luckyDirection;
    }
    
    // ==================== Getter 方法 ====================
    
    public int getWealthScore() { return wealthScore; }
    public int getCareerScore() { return careerScore; }
    public int getLoveScore() { return loveScore; }
    public int getHealthScore() { return healthScore; }
    
    public String getWealthDesc() { return wealthDesc; }
    public String getCareerDesc() { return careerDesc; }
    public String getLoveDesc() { return loveDesc; }
    public String getHealthDesc() { return healthDesc; }
    
    public String[] getLuckyColors() { return luckyColors; }
    public int[] getLuckyNumbers() { return luckyNumbers; }
    public String getLuckyTime() { return luckyTime; }
    public String getLuckyDirection() { return luckyDirection; }
    
    public String getBaziInfo() { return baziInfo; }
    public String getWuxingBalance() { return wuxingBalance; }
    public String getYongshen() { return yongshen; }
    public String getJishen() { return jishen; }
    
    public String getTodayWarning() { return todayWarning; }
    public String getTodayAdvice() { return todayAdvice; }
    
    // ==================== Setter 方法 ====================
    
    public void setWealthScore(int wealthScore) { this.wealthScore = wealthScore; }
    public void setCareerScore(int careerScore) { this.careerScore = careerScore; }
    public void setLoveScore(int loveScore) { this.loveScore = loveScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }
    
    public void setWealthDesc(String wealthDesc) { this.wealthDesc = wealthDesc; }
    public void setCareerDesc(String careerDesc) { this.careerDesc = careerDesc; }
    public void setLoveDesc(String loveDesc) { this.loveDesc = loveDesc; }
    public void setHealthDesc(String healthDesc) { this.healthDesc = healthDesc; }
    
    public void setLuckyColors(String[] luckyColors) { this.luckyColors = luckyColors; }
    public void setLuckyNumbers(int[] luckyNumbers) { this.luckyNumbers = luckyNumbers; }
    public void setLuckyTime(String luckyTime) { this.luckyTime = luckyTime; }
    public void setLuckyDirection(String luckyDirection) { this.luckyDirection = luckyDirection; }
    
    public void setBaziInfo(String baziInfo) { this.baziInfo = baziInfo; }
    public void setWuxingBalance(String wuxingBalance) { this.wuxingBalance = wuxingBalance; }
    public void setYongshen(String yongshen) { this.yongshen = yongshen; }
    public void setJishen(String jishen) { this.jishen = jishen; }
    
    public void setTodayWarning(String todayWarning) { this.todayWarning = todayWarning; }
    public void setTodayAdvice(String todayAdvice) { this.todayAdvice = todayAdvice; }
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取星级显示字符串
     * @param score 评分 (0-5)
     * @return 星级字符串
     */
    public static String getStarString(int score) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < score) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    /**
     * 获取幸运颜色字符串
     * @return 格式化的幸运颜色字符串
     */
    public String getLuckyColorsString() {
        if (luckyColors == null || luckyColors.length == 0) {
            return "红色、金色";
        }
        return String.join("、", luckyColors);
    }
    
    /**
     * 获取幸运数字字符串
     * @return 格式化的幸运数字字符串
     */
    public String getLuckyNumbersString() {
        if (luckyNumbers == null || luckyNumbers.length == 0) {
            return "3、8";
        }
        StringBuilder numbers = new StringBuilder();
        for (int i = 0; i < luckyNumbers.length; i++) {
            numbers.append(luckyNumbers[i]);
            if (i < luckyNumbers.length - 1) {
                numbers.append("、");
            }
        }
        return numbers.toString();
    }
    
    /**
     * 生成完整的运势分析字符串
     * @return 格式化的运势分析文本
     */
    public String getFormattedFortuneText() {
        StringBuilder fortune = new StringBuilder();
        fortune.append("【今日运势分析】\n\n");
        
        fortune.append("财运：").append(getStarString(wealthScore)).append(" ").append(wealthDesc).append("\n");
        fortune.append("事业：").append(getStarString(careerScore)).append(" ").append(careerDesc).append("\n");
        fortune.append("感情：").append(getStarString(loveScore)).append(" ").append(loveDesc).append("\n");
        fortune.append("健康：").append(getStarString(healthScore)).append(" ").append(healthDesc).append("\n\n");
        
        fortune.append("【开运建议】\n");
        fortune.append("吉色：").append(getLuckyColorsString()).append("\n");
        fortune.append("吉数：").append(getLuckyNumbersString()).append("\n");
        fortune.append("吉时：").append(luckyTime).append("\n");
        fortune.append("贵人方位：").append(luckyDirection);
        
        // 添加今日特殊提醒
        if (todayWarning != null && !todayWarning.isEmpty()) {
            fortune.append("\n\n【今日提醒】\n").append(todayWarning);
        }
        
        if (todayAdvice != null && !todayAdvice.isEmpty()) {
            fortune.append("\n\n【开运建议】\n").append(todayAdvice);
        }
        
        return fortune.toString();
    }
    
    /**
     * 获取总体运势评分
     * @return 总体运势评分 (0-5)
     */
    public int getOverallScore() {
        return Math.round((wealthScore + careerScore + loveScore + healthScore) / 4.0f);
    }
    
    /**
     * 获取总体运势等级描述
     * @return 运势等级描述
     */
    public String getOverallLevel() {
        int overall = getOverallScore();
        switch (overall) {
            case 5: return "运势极佳";
            case 4: return "运势良好";
            case 3: return "运势平稳";
            case 2: return "运势欠佳";
            case 1: return "运势不利";
            default: return "运势低迷";
        }
    }
}