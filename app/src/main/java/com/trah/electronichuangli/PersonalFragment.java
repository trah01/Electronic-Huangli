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
            selectedBirthDate.add(Calendar.YEAR, -30);
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
        StringBuilder fortune = new StringBuilder();
        fortune.append("【今日运势分析】\n\n");
        fortune.append("财运：★★★★☆ 财运亨通，适合投资理财\n");
        fortune.append("事业：★★★☆☆ 工作平稳，需要耐心等待时机\n");
        fortune.append("感情：★★★★★ 桃花运旺盛，单身者有望遇到良缘\n");
        fortune.append("健康：★★★☆☆ 注意饮食规律，避免熬夜\n\n");
        fortune.append("【开运建议】\n");
        fortune.append("吉色：红色、紫色\n");
        fortune.append("吉数：3、8\n");
        fortune.append("吉时：").append(birthInfo.getSimpleBirthTime()).append("时\n");
        fortune.append("贵人方位：东南方");
        
        return fortune.toString();
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