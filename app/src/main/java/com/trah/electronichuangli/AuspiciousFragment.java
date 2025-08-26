package com.trah.electronichuangli;

// ==================== Android Framework 导入 ====================
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;

// ==================== Java 标准库导入 ====================
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ==================== JSON处理 ====================
import org.json.JSONObject;

// ==================== 项目内部导入 ====================
import com.trah.electronichuangli.constants.AppConstants;
import com.trah.electronichuangli.utils.UIUtils;

/**
 * 吉日择日Fragment
 * 提供事件类型选择、天数范围选择和吉日查找功能
 * 
 * @author trah
 * @version 1.0
 */

public class AuspiciousFragment extends Fragment {
    
    // ==================== UI组件 ====================
    private Spinner eventTypeSpinner;
    private Spinner daysRangeSpinner;
    private Button searchButton;
    private LinearLayout datesContainer;
    private TextView noDatesText;
    private TextView resultsTitle;
    
    // ==================== 数据管理 ====================
    // 静态变量保持结果，切换页面不会丢失
    private static List<AuspiciousDate> persistentAuspiciousDates = new ArrayList<>();
    private static String lastSelectedEventType = "";
    private static int lastSelectedDaysRange = AppConstants.DEFAULT_DAYS_RANGE;
    private static boolean hasSearchResults = false;
    
    // ==================== 后台线程 ====================
    private ExecutorService executor;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auspicious, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        initializeViews(view);
        setupEventTypes();
        setupDaysRange();  // 新增设置天数范围
        setupSearchButton();
        
        // 恢复之前的搜索状态
        restorePreviousSearchState();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    /**
     * 初始化视图组件
     */
    private void initializeViews(View view) {
        eventTypeSpinner = view.findViewById(R.id.event_type_spinner);
        daysRangeSpinner = view.findViewById(R.id.days_range_spinner);
        searchButton = view.findViewById(R.id.search_dates_button);
        datesContainer = view.findViewById(R.id.dates_container);
        noDatesText = view.findViewById(R.id.no_dates_text);
        resultsTitle = view.findViewById(R.id.results_title);
    }
    
    /**
     * 设置事件类型选择器
     */
    private void setupEventTypes() {
        List<String> eventTypes = Arrays.asList(AppConstants.EVENT_TYPES);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(), 
            android.R.layout.simple_spinner_item, 
            eventTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);
    }
    
    /**
     * 设置天数范围选择器
     */
    private void setupDaysRange() {
        List<String> daysRanges = Arrays.asList(AppConstants.DAYS_RANGE_OPTIONS);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(), 
            android.R.layout.simple_spinner_item, 
            daysRanges
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysRangeSpinner.setAdapter(adapter);
        
        // 默认选择30天（索引为2）
        daysRangeSpinner.setSelection(2);
    }
    
    private void setupSearchButton() {
        searchButton.setOnClickListener(v -> {
            String selectedEvent = eventTypeSpinner.getSelectedItem().toString();
            String selectedRange = daysRangeSpinner.getSelectedItem().toString();
            int daysCount = Integer.parseInt(selectedRange.replace("天", ""));
            
            lastSelectedEventType = selectedEvent; // 保存当前选择的事件类型
            lastSelectedDaysRange = daysCount; // 保存当前选择的天数范围
            
            searchAuspiciousDates(selectedEvent, daysCount);
        });
    }
    
    /**
     * 恢复之前的搜索状态
     */
    private void restorePreviousSearchState() {
        if (hasSearchResults && !persistentAuspiciousDates.isEmpty()) {
            // 恢复事件类型选择
            if (!lastSelectedEventType.isEmpty()) {
                ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) eventTypeSpinner.getAdapter();
                int position = spinnerAdapter.getPosition(lastSelectedEventType);
                if (position >= 0) {
                    eventTypeSpinner.setSelection(position);
                }
            }
            
            // 恢复天数范围选择
            for (int i = 0; i < AppConstants.DAYS_RANGE_VALUES.length; i++) {
                if (AppConstants.DAYS_RANGE_VALUES[i] == lastSelectedDaysRange) {
                    daysRangeSpinner.setSelection(i);
                    break;
                }
            }
            
            // 更新UI显示之前的结果
            updateUI();
        }
    }
    
    /**
     * 搜索吉日
     */
    private void searchAuspiciousDates(String eventType, int daysRange) {
        // 清空现有数据
        persistentAuspiciousDates.clear();
        
        // 显示加载状态
        searchButton.setEnabled(false);
        searchButton.setText("正在查找...");
        
        // 异步获取黄历数据
        executor.execute(() -> generateAuspiciousDatesFromAPI(eventType, daysRange));
    }
    
    /**
     * 从本地黄历数据生成吉日列表
     */
    private void generateAuspiciousDatesFromAPI(String eventType, int daysRange) {
        List<AuspiciousDate> results = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Random random = new Random(eventType.hashCode());
        
        try {
            // 根据用户选择的天数范围获取黄历数据
            for (int i = 1; i <= daysRange; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                
                // 使用LunarHelper本地计算
                if (LunarHelper.isEventSuitable(year, month, day, eventType)) {
                    JSONObject result = LunarHelper.getHuangLiData(year, month, day);
                    
                    if (result != null) {
                        AuspiciousDate date = createAuspiciousDate(result, year, month, day, eventType, random);
                        results.add(date);
                        
                        // 找到足够多的合适日期就停止
                        if (results.size() >= AppConstants.MAX_RESULTS) {
                            break;
                        }
                    }
                }
                
                // 短暂延迟避免UI阻塞
                Thread.sleep(AppConstants.DATA_LOADING_DELAY);
            }
            
            android.util.Log.d("AuspiciousFragment", 
                String.format("在%d天内找到 %d 个适合 %s 的日期", 
                    daysRange, results.size(), eventType));
                    
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 更新UI
        updateUIWithResults(results);
    }
    
    /**
     * 创建吉日数据对象
     */
    private AuspiciousDate createAuspiciousDate(JSONObject result, int year, int month, int day, 
                                              String eventType, Random random) throws Exception {
        AuspiciousDate date = new AuspiciousDate();
        date.date = String.format("%d年%d月%d日", year, month, day);
        date.lunarDate = result.optString("nongli", "");
        date.quality = getRandomQuality(random);
        date.reason = getReasonForEvent(eventType, date.quality);
        
        String yi = result.getString("yi")
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        date.tips = "宜：" + yi.replace(",", " ");
        
        return date;
    }
    
    /**
     * 在UI线程中更新结果
     */
    private void updateUIWithResults(List<AuspiciousDate> results) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                persistentAuspiciousDates.clear();
                persistentAuspiciousDates.addAll(results);
                hasSearchResults = true;
                updateUI();
                
                searchButton.setEnabled(true);
                searchButton.setText("查找良辰吉日");
            });
        }
    }

    /**
     * 获取随机吉利等级
     */
    private String getRandomQuality(Random random) {
        String[] qualities = {"大吉", "中吉", "小吉"};
        return qualities[random.nextInt(qualities.length)];
    }
    
    /**
     * 更新UI显示结果
     */
    private void updateUI() {
        if (persistentAuspiciousDates.isEmpty()) {
            showNoResultsState();
        } else {
            showResultsState();
        }
    }
    
    /**
     * 显示无结果状态
     */
    private void showNoResultsState() {
        noDatesText.setText("未找到合适的吉日，请尝试其他事件类型");
        noDatesText.setVisibility(View.VISIBLE);
        datesContainer.setVisibility(View.GONE);
        resultsTitle.setVisibility(View.GONE);
    }
    
    /**
     * 显示结果状态
     */
    private void showResultsState() {
        noDatesText.setVisibility(View.GONE);
        resultsTitle.setVisibility(View.VISIBLE);
        datesContainer.setVisibility(View.VISIBLE);
        
        // 清空现有的子视图
        datesContainer.removeAllViews();
        
        // 为每个日期创建视图项
        for (AuspiciousDate date : persistentAuspiciousDates) {
            LinearLayout dateItem = createDateItemView(date);
            datesContainer.addView(dateItem);
        }
    }
    
    /**
     * 创建吉日视图项
     * @param date 吉日数据
     * @return 创建的视图项
     */
    private LinearLayout createDateItemView(AuspiciousDate date) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(30, 25, 30, 25);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12);
        drawable.setColor(android.graphics.Color.WHITE);
        drawable.setStroke(2, android.graphics.Color.parseColor("#E0E0E0"));
        container.setBackground(drawable);
        
        // 日期标题
        TextView dateTitle = new TextView(getContext());
        dateTitle.setText(date.date);
        dateTitle.setTextSize(18);
        dateTitle.setTextColor(android.graphics.Color.parseColor("#2C3E50"));
        dateTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(dateTitle);
        
        // 农历日期
        if (date.lunarDate != null && !date.lunarDate.isEmpty()) {
            TextView lunarDate = new TextView(getContext());
            lunarDate.setText(date.lunarDate);
            lunarDate.setTextSize(14);
            lunarDate.setTextColor(android.graphics.Color.parseColor("#7B68EE"));
            lunarDate.setPadding(0, 8, 0, 0);
            container.addView(lunarDate);
        }
        
        // 吉利等级和原因
        TextView qualityReason = new TextView(getContext());
        qualityReason.setText(date.reason);
        qualityReason.setTextSize(16);
        qualityReason.setTextColor(UIUtils.getQualityColor(date.quality));
        qualityReason.setTypeface(null, android.graphics.Typeface.BOLD);
        qualityReason.setPadding(0, 12, 0, 0);
        container.addView(qualityReason);
        
        // 建议提示
        if (date.tips != null && !date.tips.isEmpty()) {
            TextView tips = new TextView(getContext());
            tips.setText(date.tips);
            tips.setTextSize(14);
            tips.setTextColor(android.graphics.Color.parseColor("#34495E"));
            tips.setPadding(0, 8, 0, 0);
            tips.setLineSpacing(6, 1.0f);
            container.addView(tips);
        }
        
        // 设置容器布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        container.setLayoutParams(params);
        
        return container;
    }
    
    
    /**
     * 根据事件类型和吉利等级获取相应的解释原因
     * @param eventType 事件类型
     * @param quality 吉利等级
     * @return 格式化的解释原因
     */
    private String getReasonForEvent(String eventType, String quality) {
        String[] dajiReasons, zhongjiReasons, xiaojiReasons;
        
        if (eventType.equals("结婚嫁娶")) {
            dajiReasons = new String[]{"天作之合，百年好合", "良缘天定，喜结连理", "佳偶天成，永结同心"};
            zhongjiReasons = new String[]{"鸾凤和鸣，白头偕老", "缘分天成，恩爱如初", "婚姻美满，和谐幸福"};
            xiaojiReasons = new String[]{"成家立业，共建未来", "喜结良缘，相伴一生", "美好姻缘，携手同行"};
        } else if (eventType.equals("搬家入宅")) {
            dajiReasons = new String[]{"乔迁之喜，安居乐业", "新居大吉，家和万事兴", "入宅平安，财源广进"};
            zhongjiReasons = new String[]{"择址而居，人丁兴旺", "迁居顺利，家业兴隆", "新家安康，生活美满"};
            xiaojiReasons = new String[]{"搬家平稳，居住舒适", "入住新居，环境宜人", "迁移有序，生活安定"};
        } else if (eventType.equals("开业开市")) {
            dajiReasons = new String[]{"财源广进，生意兴隆", "开张大吉，日进斗金", "买卖兴隆，财运亨通"};
            zhongjiReasons = new String[]{"商机无限，利市三倍", "经营有道，财源滚滚", "开业顺利，客源不断"};
            xiaojiReasons = new String[]{"生意平稳，收入稳定", "开市有成，渐入佳境", "营业有序，逐步发展"};
        } else if (eventType.equals("动土修造")) {
            dajiReasons = new String[]{"破土动工，基业稳固", "建筑平安，工程顺利", "奠基大吉，百年大计"};
            zhongjiReasons = new String[]{"动土有时，建造无忧", "施工顺利，质量上乘", "工程有成，坚固耐用"};
            xiaojiReasons = new String[]{"动工平稳，进展有序", "修造安全，按期完成", "建设顺畅，质量合格"};
        } else if (eventType.equals("出行远行")) {
            dajiReasons = new String[]{"出行平安，一路顺风", "远行吉利，旅途无忧", "行程顺遂，贵人相助"};
            zhongjiReasons = new String[]{"征途大吉，满载而归", "旅行愉快，见闻丰富", "出门有成，平安归来"};
            xiaojiReasons = new String[]{"行程安全，路途平稳", "出行顺利，无大波折", "旅途平和，身心舒畅"};
        } else if (eventType.equals("签约合同")) {
            dajiReasons = new String[]{"签约顺利，合作共赢", "立约大吉，生意兴隆", "契约如意，信守承诺"};
            zhongjiReasons = new String[]{"协议成功，财利双收", "合作愉快，互利互惠", "签署有成，前景光明"};
            xiaojiReasons = new String[]{"合同顺利，条件公平", "协议达成，各取所需", "签约平稳，履约无忧"};
        } else if (eventType.equals("祈福祭祀")) {
            dajiReasons = new String[]{"诚心祈福，心愿必成", "祭祀吉祥，神明护佑", "虔诚礼佛，功德无量"};
            zhongjiReasons = new String[]{"祈求平安，福泽深厚", "礼拜有成，心灵安宁", "敬神得福，家庭和睦"};
            xiaojiReasons = new String[]{"祈福平和，心境宁静", "祭祀顺利，仪式圆满", "礼拜安详，精神慰藉"};
        } else if (eventType.equals("安床安门")) {
            dajiReasons = new String[]{"安床大吉，夫妻和睦", "安门纳福，家宅安宁", "家具就位，居住舒适"};
            zhongjiReasons = new String[]{"布置妥当，生活美满", "安置有序，家庭和谐", "摆设得宜，生活便利"};
            xiaojiReasons = new String[]{"安装顺利，使用方便", "摆放合理，环境整洁", "布局得当，居住舒心"};
        } else if (eventType.equals("理发剪发")) {
            dajiReasons = new String[]{"理发焕新，气质提升", "修整仪容，精神百倍", "美发吉时，形象更佳"};
            zhongjiReasons = new String[]{"造型有成，魅力倍增", "发型满意，自信倍增", "修剪得体，形象改善"};
            xiaojiReasons = new String[]{"理发顺利，效果不错", "修整头发，清爽干净", "剪发平稳，发型合适"};
        } else {
            dajiReasons = new String[]{"诸事顺利，万事如意"};
            zhongjiReasons = new String[]{"事业有成，生活美满"};
            xiaojiReasons = new String[]{"平安顺遂，一切安好"};
        }
        
        // 根据质量选择对应的理由数组
        String[] selectedReasons;
        if (quality.equals("大吉")) {
            selectedReasons = dajiReasons;
        } else if (quality.equals("中吉")) {
            selectedReasons = zhongjiReasons;
        } else {
            selectedReasons = xiaojiReasons;
        }
        
        // 根据事件类型选择不同的理由
        int index = eventType.hashCode() % selectedReasons.length;
        return quality + " - " + selectedReasons[Math.abs(index)];
    }
    
    /**
     * 根据事件类型获取相应的建议提示
     * @param eventType 事件类型
     * @return 格式化的建议提示
     */
    private String getTipsForEvent(String eventType) {
        if (eventType.equals("结婚嫁娶")) {
            return "宜：纳采、订盟、嫁娶、祭祀、祈福";
        } else if (eventType.equals("搬家入宅")) {
            return "宜：移徙、入宅、安床、开光、求嗣";
        } else if (eventType.equals("开业开市")) {
            return "宜：开市、交易、立券、纳财、开仓";
        } else if (eventType.equals("理发剪发")) {
            return "宜：理发、整容、沐浴、修整仪容";
        } else {
            return "宜：祈福、求嗣、斋醮、出行、冠笄";
        }
    }
    
    /**
     * 吉日数据类
     * 存储单个吉日的相关信息
     */
    public static class AuspiciousDate {
        /** 阳历日期 */
        public String date;
        /** 农历日期 */
        public String lunarDate;
        /** 吉利等级（大吉/中吉/小吉） */
        public String quality;
        /** 吉利原因描述 */
        public String reason;
        /** 宜忌建议提示 */
        public String tips;
    }
}