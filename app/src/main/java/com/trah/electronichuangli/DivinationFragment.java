package com.trah.electronichuangli;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.trah.electronichuangli.utils.DivinationResult;
import com.trah.electronichuangli.utils.DivinationUtils;
import com.trah.electronichuangli.utils.TrueRandomGenerator;

/**
 * 卜卦Fragment
 * 提供滑动收集随机数、实时显示随机种子、生成卦象等功能
 * 
 * @author trah
 * @version 1.0
 */
public class DivinationFragment extends Fragment implements TrueRandomGenerator.RandomDataListener {

    // ==================== UI组件 ====================
    private TextView tvCurrentSeed;
    private TextView tvTouchCoordinates;
    private TextView tvTouchSpeed;
    private TextView tvAccelerometer;
    private TextView tvGyroscope;
    private TextView tvMagnetic;
    private TextView tvNanoTime;
    private TextView tvProgressText;
    
    private View touchCollectionArea;
    private ProgressBar progressCollection;
    private Button btnResetCollection;
    private Button btnStartDivination;
    private Button btnNewDivination;
    
    // 卜卦结果显示组件
    private LinearLayout layoutDivinationResult;
    private LinearLayout layoutGuaInterpretation;
    private LinearLayout layoutFortuneAspects;
    private LinearLayout layoutNewDivination;
    private TextView tvGuaSymbol;
    private TextView tvGuaName;
    private TextView tvGuaJudgement;
    private TextView tvGuaText;
    private TextView tvGuaCi;
    private TextView tvModernInterpretation;
    private TextView tvAdvice;
    private TextView tvCareerFortune;
    private TextView tvWealthFortune;
    private TextView tvLoveFortune;
    private TextView tvHealthFortune;
    
    // ==================== 核心组件 ====================
    private TrueRandomGenerator randomGenerator;
    private Handler mainHandler;
    private DivinationResult currentResult;
    
    // ==================== 生命周期方法 ====================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_divination, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化UI组件
        initializeViews(view);
        
        // 初始化随机数生成器
        initializeRandomGenerator();
        
        // 设置事件监听器
        setupEventListeners();
        
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (randomGenerator != null) {
            randomGenerator.startSensorListening();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (randomGenerator != null) {
            randomGenerator.stopSensorListening();
        }
    }
    
    // ==================== 初始化方法 ====================
    private void initializeViews(View view) {
        // 随机数显示组件
        tvCurrentSeed = view.findViewById(R.id.tv_current_seed);
        tvTouchCoordinates = view.findViewById(R.id.tv_touch_coordinates);
        tvTouchSpeed = view.findViewById(R.id.tv_touch_speed);
        tvAccelerometer = view.findViewById(R.id.tv_accelerometer);
        tvGyroscope = view.findViewById(R.id.tv_gyroscope);
        tvMagnetic = view.findViewById(R.id.tv_magnetic);
        tvNanoTime = view.findViewById(R.id.tv_nano_time);
        tvProgressText = view.findViewById(R.id.tv_progress_text);
        
        // 交互组件
        touchCollectionArea = view.findViewById(R.id.touch_collection_area);
        progressCollection = view.findViewById(R.id.progress_collection);
        btnResetCollection = view.findViewById(R.id.btn_reset_collection);
        btnStartDivination = view.findViewById(R.id.btn_start_divination);
        btnNewDivination = view.findViewById(R.id.btn_new_divination);
        
        // 卜卦结果显示组件
        layoutDivinationResult = view.findViewById(R.id.layout_divination_result);
        layoutGuaInterpretation = view.findViewById(R.id.layout_gua_interpretation);
        layoutFortuneAspects = view.findViewById(R.id.layout_fortune_aspects);
        layoutNewDivination = view.findViewById(R.id.layout_new_divination);
        tvGuaSymbol = view.findViewById(R.id.tv_gua_symbol);
        tvGuaName = view.findViewById(R.id.tv_gua_name);
        tvGuaJudgement = view.findViewById(R.id.tv_gua_judgement);
        tvGuaText = view.findViewById(R.id.tv_gua_text);
        tvGuaCi = view.findViewById(R.id.tv_gua_ci);
        tvModernInterpretation = view.findViewById(R.id.tv_modern_interpretation);
        tvAdvice = view.findViewById(R.id.tv_advice);
        tvCareerFortune = view.findViewById(R.id.tv_career_fortune);
        tvWealthFortune = view.findViewById(R.id.tv_wealth_fortune);
        tvLoveFortune = view.findViewById(R.id.tv_love_fortune);
        tvHealthFortune = view.findViewById(R.id.tv_health_fortune);
    }
    
    private void initializeRandomGenerator() {
        if (getContext() != null) {
            randomGenerator = new TrueRandomGenerator(getContext());
            randomGenerator.setRandomDataListener(this);
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setupEventListeners() {
        // 设置滑动区域的触摸监听器
        touchCollectionArea.setOnTouchListener((v, event) -> {
            if (randomGenerator != null) {
                randomGenerator.processTouchEvent(event);
            }
            
            // 确保持续接收触摸事件
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // 请求父视图不要拦截触摸事件
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 释放触摸事件拦截
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                default:
                    return true;
            }
        });
        
        // 重新收集按钮
        btnResetCollection.setOnClickListener(v -> resetCollection());
        
        // 开始卜卦按钮
        btnStartDivination.setOnClickListener(v -> startDivination());
        
        // 重新卜卦按钮
        btnNewDivination.setOnClickListener(v -> startNewDivination());
    }
    
    // ==================== TrueRandomGenerator.RandomDataListener 实现 ====================
    @Override
    public void onRandomDataChanged(TrueRandomGenerator.RandomData randomData) {
        if (mainHandler != null) {
            mainHandler.post(() -> updateRandomDataDisplay(randomData));
        }
    }
    
    @Override
    public void onSeedCollected(long seed, String source) {
        // 种子收集时的反馈可以在这里实现，比如震动或声音
    }
    
    @Override
    public void onCollectionProgress(int collected, int target) {
        if (mainHandler != null) {
            mainHandler.post(() -> updateCollectionProgress(collected, target));
        }
    }
    
    // ==================== UI更新方法 ====================
    private void updateRandomDataDisplay(TrueRandomGenerator.RandomData data) {
        // 更新当前随机种子
        tvCurrentSeed.setText(String.valueOf(data.currentSeed));
        
        // 更新触摸坐标
        tvTouchCoordinates.setText(String.format("(%.1f, %.1f)", data.touchX, data.touchY));
        
        // 更新滑动速度
        tvTouchSpeed.setText(String.format("%.1f px/s", data.touchSpeed));
        
        // 更新加速度计数据
        tvAccelerometer.setText(String.format("X:%.2f Y:%.2f Z:%.2f", 
            data.accelerometerX, data.accelerometerY, data.accelerometerZ));
        
        // 更新陀螺仪数据
        tvGyroscope.setText(String.format("X:%.2f Y:%.2f Z:%.2f", 
            data.gyroscopeX, data.gyroscopeY, data.gyroscopeZ));
        
        // 更新磁场强度数据
        tvMagnetic.setText(String.format("X:%.1f Y:%.1f Z:%.1f", 
            data.magneticX, data.magneticY, data.magneticZ));
        
        // 更新纳秒时间
        tvNanoTime.setText(String.valueOf(data.systemNanoTime % 1000000000L));
        
        // 更新按钮状态
        btnStartDivination.setEnabled(data.isReady);
    }
    
    private void updateCollectionProgress(int collected, int target) {
        progressCollection.setProgress(collected);
        tvProgressText.setText(String.format("%d/%d", collected, target));
        
        // 当收集完成时，启用卜卦按钮
        btnStartDivination.setEnabled(collected >= target);
    }
    
    // ==================== 卜卦逻辑方法 ====================
    private void resetCollection() {
        if (randomGenerator != null) {
            randomGenerator.reset();
            progressCollection.setProgress(0);
            tvProgressText.setText("0/30");
            btnStartDivination.setEnabled(false);
            
            // 隐藏之前的卜卦结果
            layoutDivinationResult.setVisibility(View.GONE);
            layoutGuaInterpretation.setVisibility(View.GONE);
            layoutFortuneAspects.setVisibility(View.GONE);
            layoutNewDivination.setVisibility(View.GONE);
        }
    }
    
    private void startDivination() {
        if (randomGenerator != null && randomGenerator.isReady()) {
            try {
                // 生成最终随机数
                int[] randomNumbers = randomGenerator.generateFinalRandomNumbers();
                
                // 使用随机数生成卦象
                currentResult = DivinationUtils.generateDivination(randomNumbers);
                
                // 显示卜卦结果
                displayDivinationResult(currentResult);
                
            } catch (Exception e) {
                e.printStackTrace();
                // 可以显示错误信息给用户
            }
        }
    }
    
    private void startNewDivination() {
        resetCollection();
        layoutDivinationResult.setVisibility(View.GONE);
        layoutGuaInterpretation.setVisibility(View.GONE);
        layoutFortuneAspects.setVisibility(View.GONE);
        layoutNewDivination.setVisibility(View.GONE);
    }
    
    private void displayDivinationResult(DivinationResult result) {
        if (result == null) return;
        
        // 显示卦象和基本信息
        tvGuaSymbol.setText(result.getGuaSymbol());
        tvGuaName.setText(result.getGuaName());
        tvGuaJudgement.setText(result.getJudgement());
        
        // 设置判断文字颜色
        int judgementColor = getJudgementColor(result.getJudgement());
        tvGuaJudgement.setTextColor(judgementColor);
        
        // 显示卦象文本
        tvGuaText.setText(DivinationUtils.generateGuaText(result.getYaoArray()));
        
        // 显示卦辞
        tvGuaCi.setText(result.getGuaCi());
        
        // 显示现代解释
        tvModernInterpretation.setText(result.getModernInterpretation());
        
        // 显示建议
        tvAdvice.setText(result.getAdvice());
        
        // 显示各方面运势
        String[] aspects = result.getAspects();
        if (aspects != null && aspects.length >= 4) {
            tvCareerFortune.setText(aspects[0]);
            tvWealthFortune.setText(aspects[1]);
            tvLoveFortune.setText(aspects[2]);
            tvHealthFortune.setText(aspects[3]);
        }
        
        // 显示结果区域
        layoutDivinationResult.setVisibility(View.VISIBLE);
        layoutGuaInterpretation.setVisibility(View.VISIBLE);
        layoutFortuneAspects.setVisibility(View.VISIBLE);
        layoutNewDivination.setVisibility(View.VISIBLE);
    }
    
    private int getJudgementColor(String judgement) {
        if (getContext() == null) return 0xFF000000;
        
        switch (judgement) {
            case "大吉":
                return 0xFF27AE60; // 绿色
            case "中吉":
                return 0xFF3498DB; // 蓝色
            case "平":
                return 0xFFF39C12; // 橙色
            case "凶":
                return 0xFFE67E22; // 深橙色
            case "大凶":
                return 0xFFE74C3C; // 红色
            default:
                return 0xFF34495E; // 深灰色
        }
    }
    
    // ==================== 公共接口方法 ====================
    
    /**
     * 手动添加随机性（用于测试）
     */
    public void addManualRandomness() {
        if (randomGenerator != null) {
            randomGenerator.addManualRandomness();
        }
    }
    
    /**
     * 获取当前随机数收集进度
     */
    public String getCollectionProgress() {
        if (randomGenerator != null) {
            return randomGenerator.getProgress();
        }
        return "0/30";
    }
    
    /**
     * 检查是否可以开始卜卦
     */
    public boolean isReadyForDivination() {
        return randomGenerator != null && randomGenerator.isReady();
    }
    
    /**
     * 获取当前卜卦结果
     */
    public DivinationResult getCurrentResult() {
        return currentResult;
    }
}