package com.trah.electronichuangli;

// 6tail lunar-java库的正确导入
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import com.nlf.calendar.EightChar;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.List;
import com.trah.electronichuangli.utils.FortuneData;
import com.trah.electronichuangli.utils.FortuneCalculator;
import com.trah.electronichuangli.utils.PersonalInfoUtils;

/**
 * 使用6tail/lunar-java库提供黄历功能的帮助类
 * 替代原来的网络API调用，提供离线计算
 * 依赖: cn.6tail:lunar:1.7.4
 */
public class LunarHelper {
    
    /**
     * 获取指定日期的黄历信息
     * 返回格式与原API保持一致，便于现有代码使用
     */
    public static JSONObject getHuangLiData(int year, int month, int day) {
        try {
            // 创建阳历日期对象
            Solar solar = new Solar(year, month, day);
            // 转换为农历
            Lunar lunar = solar.getLunar();
            
            JSONObject result = new JSONObject();
            
            // 基本日期信息
            result.put("yangli", String.format("%d年%d月%d日", year, month, day));
            result.put("nongli", lunar.toString());
            result.put("shengxiao", lunar.getYearShengXiao());
            result.put("star", solar.getXingZuo());
            
            // 干支信息
            result.put("ganzhi", lunar.getYearInGanZhi() + "年 " + 
                              lunar.getMonthInGanZhi() + "月 " + 
                              lunar.getDayInGanZhi() + "日");
            
            // 冲煞信息（白话文解释，修正生肖对应）
            String chong = lunar.getDayChong();
            String chongDesc = "";
            if (!chong.isEmpty()) {
                String chongShengxiao = convertDizhiToShengxiao(chong);
                chongDesc = "今日冲" + chong + "，属" + chongShengxiao + "的人今天不宜办重要事情";
            }
            result.put("chong", chongDesc);
            result.put("sha", "");  // 暂时留空，避免API错误
            
            String chongShengXiao = lunar.getDayChongShengXiao();
            String suishaDesc = "";
            if (!chongShengXiao.isEmpty()) {
                suishaDesc = "岁煞方位不利，属" + chongShengXiao + "的人避免朝冲煞方向做事";
            }
            result.put("suisha", suishaDesc);
            
            // 宜忌信息
            List<String> yi = lunar.getDayYi();
            List<String> ji = lunar.getDayJi();
            
            result.put("yi", yi.toString());
            result.put("ji", ji.toString());
            
            // 方位信息 - 使用模拟数据，避免API错误
            String ganZhi = lunar.getDayInGanZhi();
            result.put("caishen", calculateCaiShenDirection(ganZhi));
            result.put("xishen", calculateXiShenDirection(ganZhi)); 
            result.put("fushen", calculateFuShenDirection(ganZhi));
            
            // 五行信息
            result.put("wuxing", lunar.getDayInGanZhi().substring(1, 2)); // 取地支作为五行
            result.put("nayin", lunar.getYearNaYin());
            
            // 节气信息
            String jieqi = lunar.getPrevJie() != null ? lunar.getPrevJie().getName() : "";
            String nextJieqi = lunar.getNextJie() != null ? lunar.getNextJie().getName() : "";
            if (!jieqi.isEmpty() && !nextJieqi.isEmpty()) {
                result.put("jieqi", "上节气：" + jieqi + " | 下节气：" + nextJieqi);
            } else if (!jieqi.isEmpty()) {
                result.put("jieqi", "节气：" + jieqi);
            } else if (!nextJieqi.isEmpty()) {
                result.put("jieqi", "下节气：" + nextJieqi);
            } else {
                result.put("jieqi", "");
            }
            
            // 吉日信息
            String jiri = getJiRiInfo(lunar);
            result.put("jiri", jiri);
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据干支计算财神方位（简化实现）
     */
    private static String calculateCaiShenDirection(String ganZhi) {
        String gan = ganZhi.substring(0, 1);
        switch (gan) {
            case "甲": case "乙": return "东北";
            case "丙": case "丁": return "正西";
            case "戊": case "己": return "正北";
            case "庚": case "辛": return "正东";
            case "壬": case "癸": return "正南";
            default: return "东北";
        }
    }
    
    /**
     * 根据干支计算喜神方位（简化实现）
     */
    private static String calculateXiShenDirection(String ganZhi) {
        String gan = ganZhi.substring(0, 1);
        switch (gan) {
            case "甲": case "己": return "东北";
            case "乙": case "庚": return "西北";
            case "丙": case "辛": return "西南";
            case "丁": case "壬": return "正南";
            case "戊": case "癸": return "东南";
            default: return "东北";
        }
    }
    
    /**
     * 根据干支计算福神方位（简化实现）
     */
    private static String calculateFuShenDirection(String ganZhi) {
        String gan = ganZhi.substring(0, 1);
        switch (gan) {
            case "甲": case "乙": return "正南";
            case "丙": case "丁": return "东南";
            case "戊": case "己": return "正东";
            case "庚": case "辛": return "西南";
            case "壬": case "癸": return "西北";
            default: return "正南";
        }
    }
    
    /**
     * 获取吉日信息
     */
    private static String getJiRiInfo(Lunar lunar) {
        List<String> festivals = lunar.getFestivals();
        List<String> otherFestivals = lunar.getOtherFestivals();
        
        StringBuilder jiri = new StringBuilder();
        
        if (!festivals.isEmpty()) {
            jiri.append(String.join(" ", festivals));
        }
        
        if (!otherFestivals.isEmpty()) {
            if (jiri.length() > 0) jiri.append(" ");
            jiri.append(String.join(" ", otherFestivals));
        }
        
        // 如果没有特殊节日，检查是否是吉日
        if (jiri.length() == 0) {
            List<String> yi = lunar.getDayYi();
            if (yi.size() >= 6) { // 宜事较多的日子认为是吉日
                jiri.append("诸事皆宜");
            } else if (yi.contains("嫁娶") || yi.contains("开市") || yi.contains("出行")) {
                jiri.append("黄道吉日");
            }
        }
        
        return jiri.toString();
    }
    
    /**
     * 检查某个事件是否适宜在指定日期进行
     */
    public static boolean isEventSuitable(int year, int month, int day, String eventType) {
        try {
            Solar solar = new Solar(year, month, day);
            Lunar lunar = solar.getLunar();
            List<String> yi = lunar.getDayYi();
            List<String> ji = lunar.getDayJi();
            
            // 事件类型映射
            String[] keywords = getEventKeywords(eventType);
            
            if (keywords != null) {
                // 精准匹配：只有宜事中包含相关关键词才认为合适
                for (String keyword : keywords) {
                    for (String yiItem : yi) {
                        if (yiItem.contains(keyword)) {
                            // 再检查是否在忌事中冲突
                            boolean hasConflict = false;
                            for (String jiItem : ji) {
                                if (jiItem.contains(keyword)) {
                                    hasConflict = true;
                                    break;
                                }
                            }
                            // 如果没有冲突，则认为合适
                            if (!hasConflict) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取事件类型对应的关键词
     */
    private static String[] getEventKeywords(String eventType) {
        switch (eventType) {
            case "结婚嫁娶":
                return new String[]{"嫁娶", "纳采", "订盟"};
            case "搬家入宅":
                return new String[]{"移徙", "入宅"};
            case "开业开市":
                return new String[]{"开市", "立券", "纳财"};
            case "动土修造":
                return new String[]{"动土", "修造"};
            case "出行远行":
                return new String[]{"出行"};
            case "签约合同":
                return new String[]{"立券", "交易"};
            case "祈福祭祀":
                return new String[]{"祈福", "祭祀"};
            case "安床安门":
                return new String[]{"安床", "安门"};
            case "理发剪发":
                return new String[]{"理发", "沐浴"};
            default:
                return null;
        }
    }
    
    /**
     * 获取生肖配对信息
     */
    public static String getZodiacCompatibility(String zodiac1, String zodiac2) {
        if (zodiac1.equals(zodiac2)) {
            return "★★★☆☆ 本命相同，需要更多理解和包容";
        }
        
        // 六合生肖（最佳配对）
        String[][] liuhe = {
            {"鼠", "牛"}, {"虎", "猪"}, {"兔", "狗"}, 
            {"龙", "鸡"}, {"蛇", "猴"}, {"马", "羊"}
        };
        
        for (String[] pair : liuhe) {
            if ((zodiac1.equals(pair[0]) && zodiac2.equals(pair[1])) ||
                (zodiac1.equals(pair[1]) && zodiac2.equals(pair[0]))) {
                return "★★★★★ 六合贵人，天作之合，感情和谐，事业互助";
            }
        }
        
        // 三合生肖
        String[][] sanhe = {
            {"鼠", "龙", "猴"}, {"牛", "蛇", "鸡"}, 
            {"虎", "马", "狗"}, {"兔", "羊", "猪"}
        };
        
        for (String[] group : sanhe) {
            boolean found1 = false, found2 = false;
            for (String animal : group) {
                if (zodiac1.equals(animal)) found1 = true;
                if (zodiac2.equals(animal)) found2 = true;
            }
            if (found1 && found2) {
                return "★★★★☆ 三合贵人，志同道合，默契度高，共同进步";
            }
        }
        
        // 六冲生肖
        String[][] liuchong = {
            {"鼠", "马"}, {"牛", "羊"}, {"虎", "猴"}, 
            {"兔", "鸡"}, {"龙", "狗"}, {"蛇", "猪"}
        };
        
        for (String[] pair : liuchong) {
            if ((zodiac1.equals(pair[0]) && zodiac2.equals(pair[1])) ||
                (zodiac1.equals(pair[1]) && zodiac2.equals(pair[0]))) {
                return "★★☆☆☆ 相冲关系，性格差异较大，需要更多包容理解";
            }
        }
        
        // 普通配对
        return "★★★☆☆ 普通配对，关系发展平稳，需要共同努力经营";
    }
    
    /**
     * 获取当前时辰信息
     */
    public static String getCurrentShiChenInfo() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        
        // 获取当前时辰
        Solar solar = new Solar(now.get(Calendar.YEAR), 
                              now.get(Calendar.MONTH) + 1, 
                              now.get(Calendar.DAY_OF_MONTH), 
                              hour, now.get(Calendar.MINUTE), 0);
        Lunar lunar = solar.getLunar();
        
        String shichen = lunar.getTimeZhi() + "时";
        String timeRange = getTimeRange(hour);
        String jixiong = getShiChenJiXiong(hour);
        String wuxing = getShiChenWuXing(lunar.getTimeZhi());
        String description = getShiChenDescription(lunar.getTimeZhi());
        
        return String.format("当前%s(%s) - %s\n五行属%s，%s", 
                           shichen, timeRange, jixiong, wuxing, description);
    }
    
    private static String getTimeRange(int hour) {
        String[] ranges = {"23-01", "01-03", "03-05", "05-07", "07-09", "09-11",
                          "11-13", "13-15", "15-17", "17-19", "19-21", "21-23"};
        int index = (hour + 1) / 2 % 12;
        return ranges[index] + "点";
    }
    
    private static String getShiChenJiXiong(int hour) {
        String[] jixiong = {"吉", "凶", "吉", "吉", "凶", "吉", 
                           "凶", "吉", "吉", "凶", "吉", "凶"};
        int index = (hour + 1) / 2 % 12;
        return jixiong[index];
    }
    
    private static String getShiChenWuXing(String zhi) {
        switch (zhi) {
            case "子": case "亥": return "水";
            case "寅": case "卯": return "木"; 
            case "巳": case "午": return "火";
            case "申": case "酉": return "金";
            case "辰": case "戌": case "丑": case "未": return "土";
            default: return "未知";
        }
    }
    
    private static String getShiChenDescription(String zhi) {
        switch (zhi) {
            case "子": return "子时主静，宜休息思考，不宜剧烈活动";
            case "丑": return "丑时易困顿，宜静养，避免重要决策";
            case "寅": return "寅时阳气始发，宜早起锻炼，精神饱满";
            case "卯": return "卯时朝阳东升，宜工作学习，效率较高";
            case "辰": return "辰时容易心浮气躁，宜保持冷静";
            case "巳": return "巳时阳气旺盛，宜处理重要事务";
            case "午": return "午时阳气极盛易急躁，宜午休调节";
            case "未": return "未时宜养神蓄力，为下午做准备";
            case "申": return "申时精神焕发，宜创作思考";
            case "酉": return "酉时宜收心养性，准备休息";
            case "戌": return "戌时易烦躁不安，宜静心修养";
            case "亥": return "亥时宜早睡，为明日蓄养精神";
            default: return "宜顺应自然规律";
        }
    }
    
    /**
     * 将地支转换为对应的生肖
     */
    private static String convertDizhiToShengxiao(String dizhi) {
        switch (dizhi) {
            case "子": return "鼠";
            case "丑": return "牛"; 
            case "寅": return "虎";
            case "卯": return "兔";
            case "辰": return "龙";
            case "巳": return "蛇";
            case "午": return "马";
            case "未": return "羊";
            case "申": return "猴";
            case "酉": return "鸡";
            case "戌": return "狗";
            case "亥": return "猪";
            default: return dizhi; // 如果不匹配就返回原值
        }
    }
    
    // ==================== 八字和个人运势相关功能 ====================
    
    /**
     * 获取指定生辰的八字信息
     * @param birthInfo 生辰信息
     * @return 八字信息字符串
     */
    public static String getBaziInfo(PersonalInfoUtils.BirthInfo birthInfo) {
        if (birthInfo == null) {
            return "未设置生辰信息";
        }
        
        try {
            // 获取出生时间对应的小时数
            int birthHour = getBirthHourFromTime(birthInfo.getSimpleBirthTime());
            
            // 创建出生时间的Solar对象
            Solar birthSolar = new Solar(birthInfo.getBirthYear(), 
                                       birthInfo.getBirthMonth(), 
                                       birthInfo.getBirthDay(), 
                                       birthHour, 0, 0);
            
            Lunar birthLunar = birthSolar.getLunar();
            EightChar eightChar = birthLunar.getEightChar();
            
            return String.format("八字：%s %s %s %s", 
                eightChar.getYear(), eightChar.getMonth(), 
                eightChar.getDay(), eightChar.getTime());
                
        } catch (Exception e) {
            e.printStackTrace();
            return "八字计算错误";
        }
    }
    
    /**
     * 根据时辰名称获取对应的小时数
     */
    private static int getBirthHourFromTime(String timeName) {
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
     * 计算五行平衡信息
     * @param birthInfo 生辰信息
     * @return 五行平衡描述
     */
    public static String getWuxingBalance(PersonalInfoUtils.BirthInfo birthInfo) {
        if (birthInfo == null) {
            return "未设置生辰信息";
        }
        
        try {
            int birthHour = getBirthHourFromTime(birthInfo.getSimpleBirthTime());
            Solar birthSolar = new Solar(birthInfo.getBirthYear(), 
                                       birthInfo.getBirthMonth(), 
                                       birthInfo.getBirthDay(), 
                                       birthHour, 0, 0);
            
            Lunar birthLunar = birthSolar.getLunar();
            EightChar eightChar = birthLunar.getEightChar();
            
            // 获取日主五行
            String dayGan = eightChar.getDay().substring(0, 1);
            String dayWuxing = getGanWuxing(dayGan);
            
            return String.format("日主属%s，五行需要%s来调和", dayWuxing, getHelperElement(dayWuxing));
            
        } catch (Exception e) {
            e.printStackTrace();
            return "五行计算错误";
        }
    }
    
    /**
     * 获取天干对应的五行
     */
    private static String getGanWuxing(String gan) {
        switch (gan) {
            case "甲": case "乙": return "木";
            case "丙": case "丁": return "火";
            case "戊": case "己": return "土";
            case "庚": case "辛": return "金";
            case "壬": case "癸": return "水";
            default: return "未知";
        }
    }
    
    /**
     * 获取辅助五行元素
     */
    private static String getHelperElement(String mainElement) {
        switch (mainElement) {
            case "木": return "水";
            case "火": return "木";
            case "土": return "火";
            case "金": return "土";
            case "水": return "金";
            default: return "平衡";
        }
    }
    
    /**
     * 获取今日个人运势
     * @param birthInfo 生辰信息
     * @return FortuneData 运势数据对象
     */
    public static FortuneData getTodayPersonalFortune(PersonalInfoUtils.BirthInfo birthInfo) {
        return FortuneCalculator.calculateTodayFortune(birthInfo);
    }
    
    /**
     * 根据个人八字获取今日宜忌建议
     * @param birthInfo 生辰信息
     * @return 个性化宜忌建议
     */
    public static String getPersonalYiJi(PersonalInfoUtils.BirthInfo birthInfo) {
        if (birthInfo == null) {
            return "请先设置个人生辰信息";
        }
        
        try {
            // 获取今日黄历信息
            Calendar today = Calendar.getInstance();
            JSONObject huangliData = getHuangLiData(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH)
            );
            
            if (huangliData == null) {
                return "黄历数据获取失败";
            }
            
            // 获取基础宜忌
            String basicYi = huangliData.optString("yi", "");
            String basicJi = huangliData.optString("ji", "");
            
            // 根据个人八字调整建议
            String personalAdvice = getPersonalizedAdvice(birthInfo);
            
            StringBuilder result = new StringBuilder();
            result.append("【今日宜忌】\n");
            result.append("宜：").append(basicYi.replace("[", "").replace("]", "")).append("\n");
            result.append("忌：").append(basicJi.replace("[", "").replace("]", "")).append("\n\n");
            result.append("【个人建议】\n").append(personalAdvice);
            
            return result.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "个性化建议生成失败";
        }
    }
    
    /**
     * 根据生辰信息生成个性化建议
     */
    private static String getPersonalizedAdvice(PersonalInfoUtils.BirthInfo birthInfo) {
        try {
            // 获取生肖
            Solar birthSolar = new Solar(birthInfo.getBirthYear(), 
                                       birthInfo.getBirthMonth(), 
                                       birthInfo.getBirthDay());
            Lunar birthLunar = birthSolar.getLunar();
            String shengxiao = birthLunar.getYearShengXiao();
            
            // 根据生肖给出建议
            switch (shengxiao) {
                case "鼠":
                    return "属鼠者今日宜早起，利于财运和事业发展";
                case "牛":
                    return "属牛者今日宜稳扎稳打，避免冒险决策";
                case "虎":
                    return "属虎者今日精力旺盛，适合处理重要事务";
                case "兔":
                    return "属兔者今日宜静不宜动，多思考少行动";
                case "龙":
                    return "属龙者今日贵人运佳，多与人交流合作";
                case "蛇":
                    return "属蛇者今日直觉敏锐，适合投资理财";
                case "马":
                    return "属马者今日行动力强，适合出行办事";
                case "羊":
                    return "属羊者今日人缘佳，适合社交活动";
                case "猴":
                    return "属猴者今日思维活跃，适合创新工作";
                case "鸡":
                    return "属鸡者今日注意力集中，适合学习进修";
                case "狗":
                    return "属狗者今日忠诚可靠，适合团队合作";
                case "猪":
                    return "属猪者今日心情愉悦，适合享受生活";
                default:
                    return "今日宜顺应自然，保持平常心";
            }
            
        } catch (Exception e) {
            return "根据您的生辰，建议今日保持积极心态";
        }
    }
}