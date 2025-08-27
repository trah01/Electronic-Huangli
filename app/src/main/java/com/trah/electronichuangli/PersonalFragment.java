package com.trah.electronichuangli;

// ==================== Android Framework 导入 ====================
import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

// ==================== Java 标准库导入 ====================
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

// ==================== 项目内部导入 ====================
import com.trah.electronichuangli.constants.AppConstants;
import com.trah.electronichuangli.utils.PersonalInfoUtils;
import com.trah.electronichuangli.utils.FortuneData;
import com.trah.electronichuangli.utils.FortuneCalculator;

/**
 * 个人信息Fragment
 * 提供个人生辰信息设置和生肖配对功能
 * 
 * @author trah
 * @version 1.0
 */
public class PersonalFragment extends Fragment {
    
    // ==================== UI组件 ====================
    private Button personalSettingButton;
    private TextView personalLuckText;
    private TextView detailedFortuneText;
    private SharedPreferences sharedPreferences;
    
    // ==================== 生肖配对相关组件 ====================
    private Spinner zodiacSpinner1, zodiacSpinner2;
    private Button zodiacMatchButton;
    private TextView compatibilityResultText;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(
            AppConstants.PREFS_BIRTH_INFO, Context.MODE_PRIVATE);
        
        initializeViews(view);
        loadPersonalInfo();
        setupPersonalSettingButton();
        setupZodiacCompatibility();
    }
    
    /**
     * 初始化视图组件
     */
    private void initializeViews(View view) {
        personalSettingButton = view.findViewById(R.id.personal_setting_button);
        personalLuckText = view.findViewById(R.id.personal_luck);
        detailedFortuneText = view.findViewById(R.id.detailed_fortune);
        
        // 初始化生肖配对相关视图
        zodiacSpinner1 = view.findViewById(R.id.zodiac_spinner_1);
        zodiacSpinner2 = view.findViewById(R.id.zodiac_spinner_2);
        zodiacMatchButton = view.findViewById(R.id.zodiac_match_button);
        compatibilityResultText = view.findViewById(R.id.compatibility_result);
    }
    
    /**
     * 设置生肖配对功能
     */
    private void setupZodiacCompatibility() {
        String[] zodiacAnimals = AppConstants.ZODIAC_ANIMALS;
        
        // 设置第一个生肖选择器
        if (zodiacSpinner1 != null) {
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, zodiacAnimals);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            zodiacSpinner1.setAdapter(adapter1);
        }
        
        // 设置第二个生肖选择器
        if (zodiacSpinner2 != null) {
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, zodiacAnimals);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            zodiacSpinner2.setAdapter(adapter2);
        }
        
        // 设置配对按钮点击事件
        if (zodiacMatchButton != null) {
            zodiacMatchButton.setOnClickListener(v -> {
                if (zodiacSpinner1 != null && zodiacSpinner2 != null) {
                    String zodiac1 = zodiacSpinner1.getSelectedItem().toString();
                    String zodiac2 = zodiacSpinner2.getSelectedItem().toString();
                    String result = calculateZodiacCompatibility(zodiac1, zodiac2);
                    
                    if (compatibilityResultText != null) {
                        compatibilityResultText.setText(result);
                        compatibilityResultText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }
    
    /**
     * 加载个人生辰信息
     */
    private void loadPersonalInfo() {
        PersonalInfoUtils.BirthInfo birthInfo = PersonalInfoUtils.loadBirthInfo(getContext());
        
        if (birthInfo != null) {
            String personalLuck = String.format("您的生辰：%d年%d月%d日 %s", 
                birthInfo.getBirthYear(), birthInfo.getBirthMonth(), birthInfo.getBirthDay(),
                birthInfo.getSimpleBirthTime());
            personalLuckText.setText(personalLuck);
            personalLuckText.setVisibility(View.VISIBLE);
            
            // 生成个性化运势
            String detailedFortune = generatePersonalFortune(birthInfo);
            detailedFortuneText.setText(detailedFortune);
            detailedFortuneText.setVisibility(View.VISIBLE);
        } else {
            personalLuckText.setText("设置个人生辰信息后将显示个性化运势分析");
            detailedFortuneText.setVisibility(View.GONE);
        }
    }
    
    /**
     * 设置个人设置按钮
     */
    private void setupPersonalSettingButton() {
        personalSettingButton.setOnClickListener(v -> showPersonalSettings());
    }
    
    /**
     * 显示个人设置对话框
     */
    private void showPersonalSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("个人生辰信息设置");
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        TextView infoText = new TextView(getContext());
        infoText.setText("请选择您的生辰信息，用于计算个人运势");
        infoText.setPadding(0, 0, 0, 20);
        layout.addView(infoText);
        
        // 出生日期选择
        TextView dateLabel = new TextView(getContext());
        dateLabel.setText("出生日期");
        layout.addView(dateLabel);
        
        Button dateButton = new Button(getContext());
        Calendar selectedBirthDate = Calendar.getInstance();
        
        // 从保存的数据中读取日期
        String savedDate = sharedPreferences.getString("birthDate", "");
        if (!savedDate.isEmpty()) {
            dateButton.setText(savedDate);
            try {
                // 简单解析保存的日期
                String[] parts = savedDate.replace("年", "-").replace("月", "-").replace("日", "").split("-");
                if (parts.length == 3) {
                    selectedBirthDate.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    selectedBirthDate.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    selectedBirthDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                }
            } catch (Exception e) {
                // 解析失败时使用默认日期
            }
        } else {
            // 默认设置为30年前的今天
            selectedBirthDate.add(Calendar.YEAR, -25);
            dateButton.setText(String.format("%d年%d月%d日",
                selectedBirthDate.get(Calendar.YEAR),
                selectedBirthDate.get(Calendar.MONTH) + 1,
                selectedBirthDate.get(Calendar.DAY_OF_MONTH)));
        }
        
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedBirthDate.set(Calendar.YEAR, year);
                    selectedBirthDate.set(Calendar.MONTH, month);
                    selectedBirthDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateButton.setText(String.format("%d年%d月%d日", year, month + 1, dayOfMonth));
                },
                selectedBirthDate.get(Calendar.YEAR),
                selectedBirthDate.get(Calendar.MONTH),
                selectedBirthDate.get(Calendar.DAY_OF_MONTH)
            );
            
            // 设置日期范围（1920年到现在）
            Calendar minDate = Calendar.getInstance();
            minDate.set(1920, 0, 1);
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            
            Calendar maxDate = Calendar.getInstance();
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            
            datePickerDialog.show();
        });
        
        layout.addView(dateButton);
        
        // 出生时辰选择
        TextView timeLabel = new TextView(getContext());
        timeLabel.setText("出生时辰");
        timeLabel.setPadding(0, 20, 0, 0);
        layout.addView(timeLabel);
        
        Spinner timeSpinner = new Spinner(getContext());
        List<String> times = Arrays.asList(
            "子时(23-01时)", "丑时(01-03时)", "寅时(03-05时)", 
            "卯时(05-07时)", "辰时(07-09时)", "巳时(09-11时)",
            "午时(11-13时)", "未时(13-15时)", "申时(15-17时)",
            "酉时(17-19时)", "戌时(19-21时)", "亥时(21-23时)"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
        
        String savedTime = sharedPreferences.getString("birthTime", "");
        if (!savedTime.isEmpty()) {
            for (int i = 0; i < times.size(); i++) {
                if (times.get(i).startsWith(savedTime)) {
                    timeSpinner.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(timeSpinner);
        
        builder.setView(layout);
        
        builder.setPositiveButton("保存", (dialog, which) -> {
            String birthTimeStr = timeSpinner.getSelectedItem().toString();
            
            // 使用PersonalInfoUtils保存生辰信息
            PersonalInfoUtils.saveBirthInfo(getContext(), selectedBirthDate, birthTimeStr);
            
            loadPersonalInfo();
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    /**
     * 生成个性化运势分析
     * @param birthInfo 生辰信息
     * @return 格式化的运势分析文本
     */
    private String generatePersonalFortune(PersonalInfoUtils.BirthInfo birthInfo) {
        if (birthInfo == null) {
            return "请先设置个人生辰信息以获取个性化运势分析";
        }
        
        try {
            // 使用FortuneCalculator计算真实运势
            FortuneData fortuneData = FortuneCalculator.calculateTodayFortune(birthInfo);
            
            if (fortuneData != null) {
                // 使用FortuneData的格式化方法生成完整运势文本
                return fortuneData.getFormattedFortuneText();
            } else {
                // 如果计算失败，返回基础运势信息
                return generateBasicFortune(birthInfo);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回基础运势信息
            return generateBasicFortune(birthInfo);
        }
    }
    
    /**
     * 生成基础运势信息（作为备用方案）
     * @param birthInfo 生辰信息
     * @return 基础运势分析文本
     */
    private String generateBasicFortune(PersonalInfoUtils.BirthInfo birthInfo) {
        StringBuilder fortune = new StringBuilder();
        fortune.append("【今日运势分析】\n\n");
        
        // 根据生辰时间简单调整运势
        String birthTime = birthInfo.getSimpleBirthTime();
        int timeModifier = getBirthTimeModifier(birthTime);
        
        // 基础运势评分（2-5星）
        int baseScore = 3;
        int wealthScore = Math.max(1, Math.min(5, baseScore + timeModifier));
        int careerScore = Math.max(1, Math.min(5, baseScore + (timeModifier + 1) % 3 - 1));
        int loveScore = Math.max(1, Math.min(5, baseScore + (timeModifier + 2) % 3 - 1));
        int healthScore = Math.max(1, Math.min(5, baseScore + (timeModifier + 3) % 3 - 1));
        
        fortune.append("财运：").append(FortuneData.getStarString(wealthScore)).append(" ");
        fortune.append(getFortuneDesc(wealthScore, "财运")).append("\n");
        
        fortune.append("事业：").append(FortuneData.getStarString(careerScore)).append(" ");
        fortune.append(getFortuneDesc(careerScore, "事业")).append("\n");
        
        fortune.append("感情：").append(FortuneData.getStarString(loveScore)).append(" ");
        fortune.append(getFortuneDesc(loveScore, "感情")).append("\n");
        
        fortune.append("健康：").append(FortuneData.getStarString(healthScore)).append(" ");
        fortune.append(getFortuneDesc(healthScore, "健康")).append("\n\n");
        
        fortune.append("【开运建议】\n");
        fortune.append("吉色：根据您的").append(birthTime).append("出生，建议今日多穿暖色系\n");
        fortune.append("吉数：").append((birthInfo.getBirthDay() % 9) + 1).append("、").append((birthInfo.getBirthMonth() % 9) + 1).append("\n");
        fortune.append("吉时：").append(birthTime).append("\n");
        fortune.append("贵人方位：根据您的生辰推算为东南方");
        
        return fortune.toString();
    }
    
    /**
     * 根据出生时辰获取运势调节因子
     */
    private int getBirthTimeModifier(String birthTime) {
        switch (birthTime) {
            case "子时": case "午时": return 1;  // 阳气最盛时辰
            case "卯时": case "酉时": return 0;  // 平衡时辰
            case "寅时": case "申时": return 2;  // 变化时辰
            case "辰时": case "戌时": return -1; // 土气时辰
            case "巳时": case "亥时": return 1;  // 火水时辰
            case "丑时": case "未时": return 0;  // 土气时辰
            default: return 0;
        }
    }
    
    /**
     * 根据评分获取运势描述
     */
    private String getFortuneDesc(int score, String type) {
        String[] levels = {"低迷", "欠佳", "平稳", "良好", "极佳"};
        String level = levels[Math.max(0, Math.min(4, score - 1))];
        
        switch (type) {
            case "财运":
                return "财运" + level + "，" + getWealthAdvice(score);
            case "事业":
                return "事业运" + level + "，" + getCareerAdvice(score);
            case "感情":
                return "感情运" + level + "，" + getLoveAdvice(score);
            case "健康":
                return "健康运" + level + "，" + getHealthAdvice(score);
            default:
                return "运势" + level;
        }
    }
    
    private String getWealthAdvice(int score) {
        switch (score) {
            case 1: return "谨慎理财，避免大额支出";
            case 2: return "保守投资，量入为出";
            case 3: return "收支平衡，可小额理财";
            case 4: return "有进账机会，适合投资";
            case 5: return "财源广进，投资理财皆宜";
            default: return "按计划理财";
        }
    }
    
    private String getCareerAdvice(int score) {
        switch (score) {
            case 1: return "工作压力大，需低调行事";
            case 2: return "工作平淡，需要耐心";
            case 3: return "工作顺利，按部就班";
            case 4: return "工作得力，有晋升机会";
            case 5: return "事业蒸蒸日上，大展宏图";
            default: return "专注工作目标";
        }
    }
    
    private String getLoveAdvice(int score) {
        switch (score) {
            case 1: return "感情需要更多沟通理解";
            case 2: return "感情需要用心经营";
            case 3: return "感情稳定，平淡见真情";
            case 4: return "感情甜蜜，单身者易遇良缘";
            case 5: return "爱情美满，有喜事临门";
            default: return "珍惜身边的感情";
        }
    }
    
    private String getHealthAdvice(int score) {
        switch (score) {
            case 1: return "注意休息调养";
            case 2: return "注意劳逸结合";
            case 3: return "保持规律作息";
            case 4: return "精力充沛，适合运动";
            case 5: return "身心健康，精神饱满";
            default: return "注意身体健康";
        }
    }
    
    /**
     * 计算生肖配对结果
     * @param zodiac1 第一个生肖
     * @param zodiac2 第二个生肖
     * @return 格式化的配对结果
     */
    private String calculateZodiacCompatibility(String zodiac1, String zodiac2) {
        // 使用LunarHelper的生肖配对功能
        String compatibility = LunarHelper.getZodiacCompatibility(zodiac1, zodiac2);
        
        StringBuilder result = new StringBuilder();
        result.append("【").append(zodiac1).append(" + ").append(zodiac2).append("】\n\n");
        result.append("配对结果：").append(compatibility);
        
        return result.toString();
    }
}