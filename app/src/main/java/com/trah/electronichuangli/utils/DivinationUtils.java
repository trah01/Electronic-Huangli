package com.trah.electronichuangli.utils;

/**
 * 卜卦算法工具类
 * 实现传统六爻八卦系统，包含完整的64卦体系
 * 
 * @author trah
 * @version 1.0
 */
public class DivinationUtils {
    
    // ==================== 六十四卦名称数组 ====================
    private static final String[] GUA_NAMES = {
        "乾卦", "坤卦", "屯卦", "蒙卦", "需卦", "讼卦", "师卦", "比卦",
        "小畜", "履卦", "泰卦", "否卦", "同人", "大有", "谦卦", "豫卦",
        "随卦", "蛊卦", "临卦", "观卦", "噬嗑", "贲卦", "剥卦", "复卦",
        "无妄", "大畜", "颐卦", "大过", "坎卦", "离卦", "咸卦", "恒卦",
        "遁卦", "大壮", "晋卦", "明夷", "家人", "睽卦", "蹇卦", "解卦",
        "损卦", "益卦", "夬卦", "姤卦", "萃卦", "升卦", "困卦", "井卦",
        "革卦", "鼎卦", "震卦", "艮卦", "渐卦", "归妹", "丰卦", "旅卦",
        "巽卦", "兑卦", "涣卦", "节卦", "中孚", "小过", "既济", "未济"
    };
    
    // ==================== 卦象符号（Unicode八卦符号）====================
    private static final String[] GUA_SYMBOLS = {
        "☰☰", "☷☷", "☳☵", "☵☶", "☰☵", "☵☰", "☷☵", "☵☷",
        "☰☴", "☱☰", "☰☷", "☷☰", "☰☲", "☲☰", "☷☶", "☳☷",
        "☱☳", "☶☴", "☱☷", "☰☴", "☲☳", "☶☲", "☷☶", "☳☷",
        "☰☳", "☶☰", "☶☳", "☱☴", "☵☵", "☲☲", "☱☶", "☳☴",
        "☰☶", "☳☰", "☲☷", "☷☲", "☴☲", "☲☱", "☵☶", "☳☵",
        "☶☱", "☴☳", "☱☰", "☰☴", "☱☷", "☷☴", "☵☱", "☴☵",
        "☱☲", "☲☴", "☳☳", "☶☶", "☴☶", "☱☳", "☲☳", "☶☲",
        "☴☴", "☱☱", "☴☵", "☱☵", "☴☶", "☳☶", "☲☵", "☵☲"
    };
    
    // ==================== 八卦基础符号 ====================
    private static final String[] BAGUA = {"☰", "☱", "☲", "☳", "☴", "☵", "☶", "☷"};
    private static final String[] BAGUA_NAMES = {"乾", "兑", "离", "震", "巽", "坎", "艮", "坤"};
    private static final String[] BAGUA_NATURE = {"天", "泽", "火", "雷", "风", "水", "山", "地"};
    
    // ==================== 卦辞数组（简化版）====================
    private static final String[] GUA_CI = {
        "元，亨，利，贞。", // 乾卦
        "元亨，利牝马之贞。", // 坤卦
        "元亨，利贞，勿用有攸往，利建侯。", // 屯卦
        "亨。匪我求童蒙，童蒙求我。", // 蒙卦
        "有孚，光亨，贞吉，利涉大川。", // 需卦
        "有孚，窒惕，中吉，终凶，利见大人，不利涉大川。", // 讼卦
        "贞，丈人，吉，无咎。", // 师卦
        "吉。原筮元永贞，无咎。", // 比卦
        // ... 为了简化，这里只展示前8个，实际应该有64个
        "小亨，密云不雨，自我西郊。", // 小畜卦
        "履虎尾，不咥人，亨。", // 履卦
        "小往大来，吉，亨。", // 泰卦
        "否之匪人，不利君子贞，大往小来。", // 否卦
        "同人于野，亨，利涉大川，利君子贞。", // 同人卦
        "元亨。", // 大有卦
        "亨，君子有终。", // 谦卦
        "利建侯行师。" // 豫卦
        // 其余卦辞为简化省略...
    };
    
    // ==================== 现代解释数组（简化版）====================
    private static final String[] MODERN_INTERPRETATIONS = {
        "乾卦象征天，代表刚健、进取、领导。事业方面积极向上，感情专一执着，财运亨通，健康强壮。", // 乾卦
        "坤卦象征地，代表柔顺、包容、承载。事业需要耐心等待，感情温和体贴，财运稳定，健康需要调养。", // 坤卦
        "屯卦象征初生，代表困难但有希望。事业起步维艰但前景光明，感情需要磨合，财运逐步改善。", // 屯卦
        "蒙卦象征启蒙，代表学习成长。事业需要学习进步，感情需要相互了解，财运通过努力获得。", // 蒙卦
        "需卦象征等待，代表耐心和时机。事业需要等待时机，感情需要真诚等待，财运需要积累。", // 需卦
        "讼卦象征争讼，代表矛盾冲突。事业可能有纷争，感情容易产生矛盾，财运波动较大。", // 讼卦
        "师卦象征军队，代表纪律和统帅。事业需要团队协作，感情需要真诚领导，财运通过合作获得。", // 师卦
        "比卦象征亲密，代表和谐团结。事业有贵人相助，感情和睦融洽，财运通过合作共赢。", // 比卦
        // 为简化，其余卦的现代解释使用通用模板
        "运势平稳，需要耐心经营，保持积极心态。", // 通用解释模板
        "形势较好，适合积极行动，把握机会。",
        "大吉之象，万事如意，前程似锦。",
        "运势低迷，需要谨慎行事，避免冲突。",
        "人际关系良好，适合合作共事。",
        "财运亨通，事业有成，家庭和睦。",
        "谦逊有益，低调行事，厚德载物。",
        "喜悦之象，心情愉快，万事顺利。"
    };
    
    // ==================== 吉凶等级 ====================
    private static final String[] LUCK_LEVELS = {
        "大吉", "中吉", "大吉", "中吉", "中吉", "凶", "平", "中吉", // 前8卦
        "平", "中吉", "大吉", "凶", "中吉", "大吉", "中吉", "中吉", // 9-16卦
        "平", "平", "中吉", "平", "平", "中吉", "凶", "中吉", // 17-24卦
        "中吉", "中吉", "平", "凶", "凶", "中吉", "平", "平", // 25-32卦
        "平", "中吉", "中吉", "凶", "中吉", "凶", "凶", "中吉", // 33-40卦
        "平", "中吉", "中吉", "平", "中吉", "中吉", "凶", "中吉", // 41-48卦
        "中吉", "中吉", "平", "平", "中吉", "平", "中吉", "平", // 49-56卦
        "中吉", "中吉", "中吉", "中吉", "中吉", "平", "中吉", "平" // 57-64卦
    };
    
    // ==================== 核心算法方法 ====================
    
    /**
     * 根据随机数生成卦象
     * @param randomNumbers 6个随机数数组
     * @return DivinationResult 卜卦结果
     */
    public static DivinationResult generateDivination(int[] randomNumbers) {
        if (randomNumbers == null || randomNumbers.length != 6) {
            throw new IllegalArgumentException("需要6个随机数来生成六爻");
        }
        
        // 生成六爻（从下到上）
        boolean[] yaoArray = new boolean[6];
        for (int i = 0; i < 6; i++) {
            // 传统算法：奇数为阳爻，偶数为阴爻
            yaoArray[i] = (randomNumbers[i] % 2) == 1;
        }
        
        // 计算卦号
        int guaNumber = calculateGuaNumber(yaoArray);
        
        // 创建结果对象
        DivinationResult result = new DivinationResult(guaNumber, GUA_NAMES[guaNumber], yaoArray);
        
        // 设置详细信息
        result.setGuaSymbol(GUA_SYMBOLS[guaNumber]);
        result.setGuaCi(getGuaCi(guaNumber));
        result.setXiangCi("象曰：" + getXiangCi(guaNumber));
        result.setModernInterpretation(getModernInterpretation(guaNumber));
        result.setJudgement(getJudgement(guaNumber));
        result.setLuckLevel(LUCK_LEVELS[guaNumber]);
        result.setAdvice(getAdvice(guaNumber));
        
        // 设置上下卦信息
        setUpperLowerGua(result, yaoArray);
        
        // 设置爻辞
        result.setYaoCi(generateYaoCi(yaoArray, guaNumber));
        
        // 设置各方面运势
        result.setAspects(generateAspects(guaNumber));
        
        return result;
    }
    
    /**
     * 计算卦号（0-63）
     */
    private static int calculateGuaNumber(boolean[] yaoArray) {
        int number = 0;
        for (int i = 0; i < 6; i++) {
            if (yaoArray[i]) { // 阳爻为1，阴爻为0
                number |= (1 << i);
            }
        }
        return number;
    }
    
    /**
     * 设置上下卦信息
     */
    private static void setUpperLowerGua(DivinationResult result, boolean[] yaoArray) {
        // 下卦（初爻、二爻、三爻）
        int lowerIndex = 0;
        if (yaoArray[0]) lowerIndex += 1;
        if (yaoArray[1]) lowerIndex += 2;
        if (yaoArray[2]) lowerIndex += 4;
        result.setLowerGua(BAGUA_NAMES[lowerIndex] + "(" + BAGUA_NATURE[lowerIndex] + ")");
        
        // 上卦（四爻、五爻、上爻）
        int upperIndex = 0;
        if (yaoArray[3]) upperIndex += 1;
        if (yaoArray[4]) upperIndex += 2;
        if (yaoArray[5]) upperIndex += 4;
        result.setUpperGua(BAGUA_NAMES[upperIndex] + "(" + BAGUA_NATURE[upperIndex] + ")");
    }
    
    /**
     * 获取卦辞
     */
    private static String getGuaCi(int guaNumber) {
        if (guaNumber < GUA_CI.length) {
            return GUA_CI[guaNumber];
        }
        return "此卦吉凶参半，需谨慎行事。";
    }
    
    /**
     * 获取象辞
     */
    private static String getXiangCi(int guaNumber) {
        // 简化的象辞生成
        String[] xiangTemplates = {
            "天行健，君子以自强不息",
            "地势坤，君子以厚德载物",
            "云雷屯，君子以经纶",
            "山下出泉蒙，君子以果行育德",
            "云上于天需，君子以饮食宴乐",
            "天与水违行讼，君子以作事谋始"
        };
        
        if (guaNumber < xiangTemplates.length) {
            return xiangTemplates[guaNumber];
        }
        return "此象提示需要顺应天时，把握时机。";
    }
    
    /**
     * 获取现代解释
     */
    private static String getModernInterpretation(int guaNumber) {
        if (guaNumber < MODERN_INTERPRETATIONS.length) {
            return MODERN_INTERPRETATIONS[guaNumber];
        }
        return "运势起伏不定，需要保持平常心，顺其自然发展。";
    }
    
    /**
     * 获取判断（吉凶）
     */
    private static String getJudgement(int guaNumber) {
        return LUCK_LEVELS[guaNumber];
    }
    
    /**
     * 获取建议
     */
    private static String getAdvice(int guaNumber) {
        switch (LUCK_LEVELS[guaNumber]) {
            case "大吉":
                return "运势极佳，可大胆行事，把握机遇，积极进取。";
            case "中吉":
                return "运势良好，适合稳步前进，保持现状，小心谨慎。";
            case "平":
                return "运势平稳，宜守不宜攻，保持耐心，等待时机。";
            case "凶":
                return "运势不佳，需要谨慎行事，避免冒险，静待转机。";
            case "大凶":
                return "运势极差，宜退守保身，避免一切重大决策。";
            default:
                return "保持平常心，顺应自然规律。";
        }
    }
    
    /**
     * 生成爻辞
     */
    private static String[] generateYaoCi(boolean[] yaoArray, int guaNumber) {
        String[] yaoCi = new String[6];
        String[] positions = {"初", "二", "三", "四", "五", "上"};
        
        for (int i = 0; i < 6; i++) {
            String yaoType = yaoArray[i] ? "九" : "六";
            yaoCi[i] = yaoType + positions[i] + "：" + getYaoInterpretation(i, yaoArray[i], guaNumber);
        }
        
        return yaoCi;
    }
    
    /**
     * 获取爻的解释
     */
    private static String getYaoInterpretation(int position, boolean isYang, int guaNumber) {
        String[] yangTemplates = {"潜龙勿用", "见龙在田", "君子终日乾乾", "或跃在渊", "飞龙在天", "亢龙有悔"};
        String[] yinTemplates = {"履霜坚冰至", "直方大不习无不利", "含章可贞", "括囊无咎无誉", "黄裳元吉", "龙战于野"};
        
        if (isYang && position < yangTemplates.length) {
            return yangTemplates[position];
        } else if (!isYang && position < yinTemplates.length) {
            return yinTemplates[position];
        }
        
        return isYang ? "阳爻得位，吉。" : "阴爻得位，吉。";
    }
    
    /**
     * 生成各方面运势
     */
    private static String[] generateAspects(int guaNumber) {
        String[] aspects = new String[4];
        String luckLevel = LUCK_LEVELS[guaNumber];
        
        // 事业运势
        aspects[0] = "事业：" + getAspectDescription(luckLevel, "事业");
        
        // 财运
        aspects[1] = "财运：" + getAspectDescription(luckLevel, "财运");
        
        // 感情
        aspects[2] = "感情：" + getAspectDescription(luckLevel, "感情");
        
        // 健康
        aspects[3] = "健康：" + getAspectDescription(luckLevel, "健康");
        
        return aspects;
    }
    
    /**
     * 根据运势等级获取具体方面的描述
     */
    private static String getAspectDescription(String luckLevel, String aspect) {
        switch (luckLevel) {
            case "大吉":
                switch (aspect) {
                    case "事业": return "事业蒸蒸日上，有贵人相助，适合扩展业务";
                    case "财运": return "财运亨通，投资理财皆宜，收入丰厚";
                    case "感情": return "感情甜蜜美满，有喜事临门，单身者易遇良缘";
                    case "健康": return "身体健康，精力充沛，适合运动锻炼";
                }
                break;
            case "中吉":
                switch (aspect) {
                    case "事业": return "事业稳步发展，有小的进展，需要继续努力";
                    case "财运": return "财运较好，收支平衡，可适当投资";
                    case "感情": return "感情和谐稳定，相处愉快，需要用心经营";
                    case "健康": return "健康状况良好，注意休息调养";
                }
                break;
            case "平":
                switch (aspect) {
                    case "事业": return "事业平稳，按部就班，需要耐心等待";
                    case "财运": return "财运平平，收支基本持平，避免大额支出";
                    case "感情": return "感情平淡，需要多沟通交流，增进感情";
                    case "健康": return "健康平稳，注意日常保养";
                }
                break;
            case "凶":
                switch (aspect) {
                    case "事业": return "事业遇阻，工作压力大，需要低调行事";
                    case "财运": return "财运不佳，容易破财，避免投资冒险";
                    case "感情": return "感情波动较大，容易产生矛盾，需要包容理解";
                    case "健康": return "健康需要注意，避免过度劳累";
                }
                break;
        }
        return "需要谨慎对待，保持平常心";
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 根据卦号获取卦名
     */
    public static String getGuaName(int guaNumber) {
        if (guaNumber >= 0 && guaNumber < GUA_NAMES.length) {
            return GUA_NAMES[guaNumber];
        }
        return "未知卦";
    }
    
    /**
     * 根据爻数组生成卦象文本显示
     */
    public static String generateGuaText(boolean[] yaoArray) {
        StringBuilder sb = new StringBuilder();
        // 从上爻到下爻显示（传统显示方式）
        for (int i = 5; i >= 0; i--) {
            if (yaoArray[i]) {
                sb.append("━━━ (阳爻)\n"); // 阳爻：连续线
            } else {
                sb.append("━ ━ (阴爻)\n"); // 阴爻：断开线
            }
        }
        return sb.toString().trim();
    }
    
    /**
     * 验证卦象的合法性
     */
    public static boolean isValidGua(boolean[] yaoArray) {
        return yaoArray != null && yaoArray.length == 6;
    }
}