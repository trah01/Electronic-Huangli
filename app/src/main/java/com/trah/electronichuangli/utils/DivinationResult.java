package com.trah.electronichuangli.utils;

import java.util.Date;

/**
 * 卜卦结果数据模型
 * 存储卦象、爻辞、解释等完整的卜卦信息
 * 
 * @author trah
 * @version 1.0
 */
public class DivinationResult {
    
    // ==================== 基础卦象信息 ====================
    private int guaNumber;              // 卦号 (0-63)
    private String guaName;             // 卦名（如：乾卦）
    private String guaSymbol;           // 卦象符号
    private String upperGua;            // 上卦
    private String lowerGua;            // 下卦
    private boolean[] yaoArray;         // 六爻数组，true为阳爻，false为阴爻
    
    // ==================== 卦辞解释 ====================
    private String guaCi;               // 卦辞
    private String xiangCi;             // 象辞  
    private String[] yaoCi;             // 六爻爻辞
    private String judgement;           // 判断（吉凶）
    
    // ==================== 现代解释 ====================
    private String modernInterpretation; // 现代白话解释
    private String luckLevel;           // 运势等级（大吉、中吉、平、凶、大凶）
    private String advice;              // 建议
    private String[] aspects;           // 各方面运势（事业、财运、感情、健康）
    
    // ==================== 随机数生成信息 ====================
    private long[] randomSeeds;         // 随机种子数组
    private String seedSource;          // 随机数来源描述
    private Date divinationTime;        // 卜卦时间
    private String location;            // 卜卦地点（可选）
    
    // ==================== 构造函数 ====================
    public DivinationResult() {
        this.yaoArray = new boolean[6];
        this.yaoCi = new String[6];
        this.aspects = new String[4];
        this.divinationTime = new Date();
    }
    
    public DivinationResult(int guaNumber, String guaName, boolean[] yaoArray) {
        this();
        this.guaNumber = guaNumber;
        this.guaName = guaName;
        this.yaoArray = yaoArray.clone();
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public int getGuaNumber() {
        return guaNumber;
    }
    
    public void setGuaNumber(int guaNumber) {
        this.guaNumber = guaNumber;
    }
    
    public String getGuaName() {
        return guaName;
    }
    
    public void setGuaName(String guaName) {
        this.guaName = guaName;
    }
    
    public String getGuaSymbol() {
        return guaSymbol;
    }
    
    public void setGuaSymbol(String guaSymbol) {
        this.guaSymbol = guaSymbol;
    }
    
    public String getUpperGua() {
        return upperGua;
    }
    
    public void setUpperGua(String upperGua) {
        this.upperGua = upperGua;
    }
    
    public String getLowerGua() {
        return lowerGua;
    }
    
    public void setLowerGua(String lowerGua) {
        this.lowerGua = lowerGua;
    }
    
    public boolean[] getYaoArray() {
        return yaoArray;
    }
    
    public void setYaoArray(boolean[] yaoArray) {
        this.yaoArray = yaoArray.clone();
    }
    
    public String getGuaCi() {
        return guaCi;
    }
    
    public void setGuaCi(String guaCi) {
        this.guaCi = guaCi;
    }
    
    public String getXiangCi() {
        return xiangCi;
    }
    
    public void setXiangCi(String xiangCi) {
        this.xiangCi = xiangCi;
    }
    
    public String[] getYaoCi() {
        return yaoCi;
    }
    
    public void setYaoCi(String[] yaoCi) {
        this.yaoCi = yaoCi.clone();
    }
    
    public String getJudgement() {
        return judgement;
    }
    
    public void setJudgement(String judgement) {
        this.judgement = judgement;
    }
    
    public String getModernInterpretation() {
        return modernInterpretation;
    }
    
    public void setModernInterpretation(String modernInterpretation) {
        this.modernInterpretation = modernInterpretation;
    }
    
    public String getLuckLevel() {
        return luckLevel;
    }
    
    public void setLuckLevel(String luckLevel) {
        this.luckLevel = luckLevel;
    }
    
    public String getAdvice() {
        return advice;
    }
    
    public void setAdvice(String advice) {
        this.advice = advice;
    }
    
    public String[] getAspects() {
        return aspects;
    }
    
    public void setAspects(String[] aspects) {
        this.aspects = aspects.clone();
    }
    
    public long[] getRandomSeeds() {
        return randomSeeds;
    }
    
    public void setRandomSeeds(long[] randomSeeds) {
        this.randomSeeds = randomSeeds.clone();
    }
    
    public String getSeedSource() {
        return seedSource;
    }
    
    public void setSeedSource(String seedSource) {
        this.seedSource = seedSource;
    }
    
    public Date getDivinationTime() {
        return divinationTime;
    }
    
    public void setDivinationTime(Date divinationTime) {
        this.divinationTime = divinationTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取卦象的文本表示
     * @return 卦象文本（如：☰☷）
     */
    public String getGuaText() {
        if (guaSymbol != null && !guaSymbol.isEmpty()) {
            return guaSymbol;
        }
        
        StringBuilder sb = new StringBuilder();
        // 从上爻到下爻显示
        for (int i = 5; i >= 0; i--) {
            sb.append(yaoArray[i] ? "━━━" : "━ ━").append("\n");
        }
        return sb.toString().trim();
    }
    
    /**
     * 获取简短的卦象描述
     * @return 简短描述
     */
    public String getShortDescription() {
        return String.format("%s - %s", guaName, judgement);
    }
    
    /**
     * 获取详细的卦象信息
     * @return 详细信息字符串
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("卦名：").append(guaName).append("\n");
        sb.append("卦辞：").append(guaCi).append("\n\n");
        sb.append("象辞：").append(xiangCi).append("\n\n");
        sb.append("现代解释：").append(modernInterpretation).append("\n\n");
        sb.append("建议：").append(advice);
        return sb.toString();
    }
    
    /**
     * 获取运势评分（1-100）
     * @return 评分
     */
    public int getLuckScore() {
        switch (luckLevel) {
            case "大吉":
                return 90 + (guaNumber % 10);
            case "中吉":
                return 70 + (guaNumber % 20);
            case "平":
                return 50 + (guaNumber % 20);
            case "凶":
                return 30 + (guaNumber % 20);
            case "大凶":
                return 10 + (guaNumber % 20);
            default:
                return 50;
        }
    }
    
    /**
     * 获取卦象的五行属性
     * @return 五行属性
     */
    public String getWuxingProperty() {
        // 根据卦号简化计算五行属性
        String[] wuxing = {"金", "木", "水", "火", "土"};
        return wuxing[guaNumber % 5];
    }
    
    @Override
    public String toString() {
        return String.format("DivinationResult{guaName='%s', judgement='%s', luckLevel='%s'}", 
                           guaName, judgement, luckLevel);
    }
}