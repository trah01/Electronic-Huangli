package com.trah.electronichuangli.utils;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import com.nlf.calendar.LunarTime;
import com.nlf.calendar.EightChar;
import java.util.Calendar;
import java.util.Random;

/**
 * 运势计算工具类
 * 基于生辰八字和当前日期计算个人运势
 * 
 * @author trah
 * @version 1.0
 */
public class FortuneCalculator {
    
    private static final Random random = new Random();
    
    /**
     * 根据生辰信息计算今日运势
     * @param birthInfo 生辰信息
     * @return FortuneData 运势数据
     */
    public static FortuneData calculateTodayFortune(PersonalInfoUtils.BirthInfo birthInfo) {
        if (birthInfo == null) {
            return createDefaultFortune();
        }
        
        try {
            // 获取出生日期的农历对象
            Solar birthSolar = new Solar(birthInfo.getBirthYear(), 
                                       birthInfo.getBirthMonth(), 
                                       birthInfo.getBirthDay());
            Lunar birthLunar = birthSolar.getLunar();
            
            // 获取今日农历对象
            Calendar today = Calendar.getInstance();
            Solar todaySolar = new Solar(today.get(Calendar.YEAR), 
                                       today.get(Calendar.MONTH) + 1, 
                                       today.get(Calendar.DAY_OF_MONTH));
            Lunar todayLunar = todaySolar.getLunar();
            
            // 创建运势数据对象
            FortuneData fortune = new FortuneData();
            
            // 计算八字信息
            calculateBaziInfo(birthInfo, fortune);
            
            // 计算各项运势
            calculateWealthFortune(birthLunar, todayLunar, fortune);
            calculateCareerFortune(birthLunar, todayLunar, fortune);
            calculateLoveFortune(birthLunar, todayLunar, fortune);
            calculateHealthFortune(birthLunar, todayLunar, fortune);
            
            // 计算开运建议
            calculateLuckyElements(birthLunar, todayLunar, birthInfo, fortune);
            
            // 生成今日特殊建议
            generateTodayAdvice(birthLunar, todayLunar, fortune);
            
            return fortune;
            
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultFortune();
        }
    }
    
    /**
     * 计算八字信息
     */
    private static void calculateBaziInfo(PersonalInfoUtils.BirthInfo birthInfo, FortuneData fortune) {
        try {
            // 创建出生时间的八字对象
            int birthHour = getBirthHour(birthInfo.getSimpleBirthTime());
            Solar birthSolar = new Solar(birthInfo.getBirthYear(), 
                                       birthInfo.getBirthMonth(), 
                                       birthInfo.getBirthDay(), 
                                       birthHour, 0, 0);
            
            Lunar birthLunar = birthSolar.getLunar();
            EightChar eightChar = birthLunar.getEightChar();
            
            // 设置八字信息
            String baziInfo = String.format("八字：%s %s %s %s", 
                eightChar.getYear(), eightChar.getMonth(), 
                eightChar.getDay(), eightChar.getTime());
            fortune.setBaziInfo(baziInfo);
            
            // 计算五行平衡
            String wuxingBalance = calculateWuxingBalance(eightChar);
            fortune.setWuxingBalance(wuxingBalance);
            
            // 设置用神和忌神（简化计算）
            String[] yongjishen = calculateYongJishen(eightChar);
            fortune.setYongshen(yongjishen[0]);
            fortune.setJishen(yongjishen[1]);
            
        } catch (Exception e) {
            fortune.setBaziInfo("八字信息计算中...");
            fortune.setWuxingBalance("五行平衡");
        }
    }
    
    /**
     * 根据时辰名称获取对应小时
     */
    private static int getBirthHour(String timeName) {
        switch (timeName) {
            case "子时": return 0;
            case "丑时": return 2;
            case "寅时": return 4;
            case "卯时": return 6;
            case "辰时": return 8;
            case "巳时": return 10;
            case "午时": return 12;
            case "未时": return 14;
            case "申时": return 16;
            case "酉时": return 18;
            case "戌时": return 20;
            case "亥时": return 22;
            default: return 0;
        }
    }
    
    /**
     * 计算五行平衡
     */
    private static String calculateWuxingBalance(EightChar eightChar) {
        // 简化的五行统计
        String[] elements = {"金", "木", "水", "火", "土"};
        int[] counts = new int[5];
        
        // 统计八字中五行出现次数（简化实现）
        String bazi = eightChar.getYear() + eightChar.getMonth() + 
                     eightChar.getDay() + eightChar.getTime();
        
        // 这里可以更精确地计算五行，目前使用简化方式
        for (String element : elements) {
            counts[getElementIndex(element)] = 1 + random.nextInt(3);
        }
        
        // 找出最强和最弱的五行
        int maxIndex = 0, minIndex = 0;
        for (int i = 1; i < 5; i++) {
            if (counts[i] > counts[maxIndex]) maxIndex = i;
            if (counts[i] < counts[minIndex]) minIndex = i;
        }
        
        return String.format("五行：%s偏旺，%s偏弱", elements[maxIndex], elements[minIndex]);
    }
    
    /**
     * 获取五行元素索引
     */
    private static int getElementIndex(String element) {
        switch (element) {
            case "金": return 0;
            case "木": return 1;
            case "水": return 2;
            case "火": return 3;
            case "土": return 4;
            default: return 0;
        }
    }
    
    /**
     * 计算用神和忌神
     */
    private static String[] calculateYongJishen(EightChar eightChar) {
        String[] elements = {"金", "木", "水", "火", "土"};
        String yongshen = elements[random.nextInt(5)];
        String jishen = elements[random.nextInt(5)];
        while (jishen.equals(yongshen)) {
            jishen = elements[random.nextInt(5)];
        }
        return new String[]{yongshen, jishen};
    }
    
    /**
     * 计算财运
     */
    private static void calculateWealthFortune(Lunar birthLunar, Lunar todayLunar, FortuneData fortune) {
        // 基于生肖和今日干支计算财运
        String birthShengxiao = birthLunar.getYearShengXiao();
        String todayGanzhi = todayLunar.getDayInGanZhi();
        
        int wealthScore = calculateBasicScore(birthShengxiao, todayGanzhi, "财");
        
        String[] wealthDescs = {
            "财运低迷，开支需谨慎，避免大额投资",
            "财运欠佳，收入一般，宜保守理财",
            "财运平稳，收支平衡，可小额投资",
            "财运不错，有进账机会，适合理财规划",
            "财运亨通，财源广进，投资理财皆宜"
        };
        
        fortune.setWealthScore(wealthScore);
        fortune.setWealthDesc(wealthDescs[wealthScore - 1]);
    }
    
    /**
     * 计算事业运势
     */
    private static void calculateCareerFortune(Lunar birthLunar, Lunar todayLunar, FortuneData fortune) {
        String birthShengxiao = birthLunar.getYearShengXiao();
        String todayGanzhi = todayLunar.getDayInGanZhi();
        
        int careerScore = calculateBasicScore(birthShengxiao, todayGanzhi, "官");
        
        String[] careerDescs = {
            "事业阻滞，工作压力大，需低调行事",
            "事业平淡，工作一般，需要耐心等待",
            "事业平稳，工作顺利，按部就班即可",
            "事业有进展，工作得力，有晋升机会",
            "事业蒸蒸日上，工作顺风顺水，大展宏图"
        };
        
        fortune.setCareerScore(careerScore);
        fortune.setCareerDesc(careerDescs[careerScore - 1]);
    }
    
    /**
     * 计算感情运势
     */
    private static void calculateLoveFortune(Lunar birthLunar, Lunar todayLunar, FortuneData fortune) {
        String birthShengxiao = birthLunar.getYearShengXiao();
        String todayGanzhi = todayLunar.getDayInGanZhi();
        
        int loveScore = calculateBasicScore(birthShengxiao, todayGanzhi, "桃花");
        
        String[] loveDescs = {
            "感情冷淡，易生口角，需要更多沟通理解",
            "感情平淡，缺乏激情，需要用心经营",
            "感情稳定，相处和谐，平淡中见真情",
            "感情甜蜜，桃花运旺，单身者易遇良缘",
            "感情如蜜，爱情美满，有喜事临门"
        };
        
        fortune.setLoveScore(loveScore);
        fortune.setLoveDesc(loveDescs[loveScore - 1]);
    }
    
    /**
     * 计算健康运势
     */
    private static void calculateHealthFortune(Lunar birthLunar, Lunar todayLunar, FortuneData fortune) {
        String birthShengxiao = birthLunar.getYearShengXiao();
        String todayGanzhi = todayLunar.getDayInGanZhi();
        
        int healthScore = calculateBasicScore(birthShengxiao, todayGanzhi, "食伤");
        
        String[] healthDescs = {
            "健康欠佳，体质较弱，需注意休息调养",
            "健康一般，易疲劳，注意劳逸结合",
            "健康平稳，精神尚可，保持规律作息",
            "健康良好，精力充沛，适合运动锻炼",
            "健康极佳，身强体壮，精神饱满"
        };
        
        fortune.setHealthScore(healthScore);
        fortune.setHealthDesc(healthDescs[healthScore - 1]);
    }
    
    /**
     * 基础评分计算方法
     */
    private static int calculateBasicScore(String shengxiao, String ganzhi, String type) {
        // 基于生肖和干支的简化算法
        int baseScore = 3; // 基础分
        
        // 根据生肖调整
        int shengxiaoModifier = shengxiao.hashCode() % 3 - 1; // -1, 0, 1
        
        // 根据干支调整
        int ganzhiModifier = ganzhi.hashCode() % 3 - 1; // -1, 0, 1
        
        // 根据类型微调
        int typeModifier = type.hashCode() % 3 - 1; // -1, 0, 1
        
        int finalScore = baseScore + shengxiaoModifier + ganzhiModifier + typeModifier;
        
        // 确保分数在1-5范围内
        return Math.max(1, Math.min(5, finalScore));
    }
    
    /**
     * 计算开运元素
     */
    private static void calculateLuckyElements(Lunar birthLunar, Lunar todayLunar, 
                                             PersonalInfoUtils.BirthInfo birthInfo, FortuneData fortune) {
        // 计算幸运颜色（基于用神五行）
        String[] allColors = {"红色", "黄色", "白色", "黑色", "绿色", "紫色", "金色", "银色"};
        String[] luckyColors = {
            allColors[birthInfo.getBirthYear() % allColors.length],
            allColors[(birthInfo.getBirthMonth() * 2) % allColors.length]
        };
        fortune.setLuckyColors(luckyColors);
        
        // 计算幸运数字
        int[] luckyNumbers = {
            (birthInfo.getBirthYear() % 9) + 1,
            (birthInfo.getBirthMonth() * birthInfo.getBirthDay()) % 9 + 1
        };
        fortune.setLuckyNumbers(luckyNumbers);
        
        // 设置吉时（结合出生时辰）
        String luckyTime = birthInfo.getSimpleBirthTime();
        fortune.setLuckyTime(luckyTime);
        
        // 计算贵人方位
        String[] directions = {"东方", "南方", "西方", "北方", "东南", "西南", "东北", "西北"};
        String luckyDirection = directions[(birthInfo.getBirthYear() + birthInfo.getBirthMonth()) % directions.length];
        fortune.setLuckyDirection(luckyDirection);
    }
    
    /**
     * 生成今日特殊建议
     */
    private static void generateTodayAdvice(Lunar birthLunar, Lunar todayLunar, FortuneData fortune) {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        
        // 根据星期和生肖生成建议
        String[] weeklyAdvice = {
            "今日宜静不宜动，适合思考规划",
            "今日适合社交活动，多与人交流",
            "今日工作效率高，处理重要事务",
            "今日财运较好，可关注投资机会",
            "今日感情运佳，适合约会聚会",
            "今日健康运好，适合运动锻炼",
            "今日宜休养生息，避免重大决定"
        };
        
        fortune.setTodayAdvice(weeklyAdvice[dayOfWeek - 1]);
        
        // 生成警告信息（如果有冲煞）
        String chongShengxiao = todayLunar.getDayChongShengXiao();
        String birthShengxiao = birthLunar.getYearShengXiao();
        
        if (chongShengxiao != null && chongShengxiao.equals(birthShengxiao)) {
            fortune.setTodayWarning("今日冲" + birthShengxiao + "，您需要特别谨慎，避免重要决策和大额消费");
        }
    }
    
    /**
     * 创建默认运势（当计算失败时使用）
     */
    private static FortuneData createDefaultFortune() {
        FortuneData fortune = new FortuneData();
        fortune.setWealthScore(3);
        fortune.setCareerScore(3);
        fortune.setLoveScore(3);
        fortune.setHealthScore(3);
        
        fortune.setWealthDesc("财运平稳，收支基本平衡");
        fortune.setCareerDesc("事业平稳，工作按部就班");
        fortune.setLoveDesc("感情稳定，相处和谐");
        fortune.setHealthDesc("健康平稳，注意保养");
        
        fortune.setTodayAdvice("今日宜保持平常心，踏实做事");
        
        return fortune;
    }
}