package com.trah.electronichuangli;

// ==================== Android Framework 导入 ====================
import static com.trah.electronichuangli.constants.AppConstants.COMPASS_PRECISION_THRESHOLD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.widget.Button;

// ==================== 分享功能相关导入 ====================
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.Gravity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.net.Uri;
import androidx.core.content.FileProvider;

// ==================== 个人设置相关导入 ====================
import android.content.Intent;
import android.app.AlertDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.Arrays;

import android.app.DatePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;

// ==================== JSON 和工具类导入 ====================
import org.json.JSONObject;

// ==================== 项目内部导入 ====================
import com.trah.electronichuangli.constants.AppConstants;
import com.trah.electronichuangli.utils.CompassUtils;
import com.trah.electronichuangli.utils.PersonalInfoUtils;
import com.trah.electronichuangli.utils.UIUtils;

/**
 * 主Activity
 * 管理Fragment导航和HomeFragment的传感器功能
 * 
 * @author trah
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // ==================== UI组件 ====================
    private ImageView xishenPointer, fushenPointer, caishenPointer;
    private TextView yiTextView, jiTextView, dateView, fushenText, xishenText, caishenText;
    private TextView compassDegreeText;
    private TextView lunarDateText, shengxiaoText, jieqiText, chongshaText;
    private TextView wuxingText, shichenText;
    private TextView personalLuckText, detailedFortuneText;
    private ProgressBar loadingProgress;
    private ImageButton datePickerButton, shareButton;
    private Button personalSettingButton;
    
    // ==================== 数据相关 ====================
    private Calendar selectedDate;
    private PersonalInfoUtils.BirthInfo birthInfo;
    private Calendar birthDate;  // 用户设置的出生日期
    private String birthTime = AppConstants.DEFAULT_BIRTH_TIME;  // 用户设置的出生时辰
    
    // ==================== 传感器相关 ====================
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private long lastUpdateTime = 0;
    private float currentAzimuth = AppConstants.UNINITIALIZED_AZIMUTH;
    private float lastValidAzimuth = 0f;
    private float caishenDegrees, xishenDegrees, fushenDegrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        configureStatusBar();

        // 初始化底部导航
        setupBottomNavigation();
        
        // 默认显示首页
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    /**
     * 设置底部导航栏
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_auspicious) {
                fragment = new AuspiciousFragment();
            } else if (item.getItemId() == R.id.nav_personal) {
                fragment = new PersonalFragment();
            }
            
            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }
    
    /**
     * 加载Fragment
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    /**
     * 初始化HomeFragment（对外接口）
     */
    public void initializeHomeFragment() {
        initializeHomeFragment(null);
    }
    
    /**
     * 初始化HomeFragment
     * @param fragmentView Fragment的视图（可为null）
     */
    public void initializeHomeFragment(View fragmentView) {
        View rootView = fragmentView != null ? fragmentView : findViewById(android.R.id.content);
        
        // 初始化传感器
        initializeSensors();
        
        // 初始化UI
        initializeUI(rootView);
        
        // 加载个人信息
        loadBirthInfo();
        
        // 启动传感器监听
        startSensorListening();
        
        // 异步获取黄历数据
        new Thread(this::fetchAndSetData).start();
    }

    /**
     * 配置状态栏
     */
    private void configureStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void initializeUI() {
        // 为了兼容性，调用带参数的版本
        initializeUI(findViewById(android.R.id.content));
    }
    
    private void initializeUI(View rootView) {
        caishenPointer = rootView.findViewById(R.id.caishen_pointer);
        xishenPointer = rootView.findViewById(R.id.xishen_pointer);
        fushenPointer = rootView.findViewById(R.id.fushen_pointer);
        
        // 设置指针初始状态 - 分散显示避免重叠
        if (caishenPointer != null) {
            caishenPointer.setAlpha(0.3f); // 半透明表示加载中
            caishenPointer.setRotation(0); // 指向北
        }
        if (xishenPointer != null) {
            xishenPointer.setAlpha(0.3f);
            xishenPointer.setRotation(120); // 指向东南
        }
        if (fushenPointer != null) {
            fushenPointer.setAlpha(0.3f);
            fushenPointer.setRotation(240); // 指向西南
        }
        
        yiTextView = rootView.findViewById(R.id.yi);
        jiTextView = rootView.findViewById(R.id.ji);
        xishenText = rootView.findViewById(R.id.xishen_fanwei);
        caishenText = rootView.findViewById(R.id.caisen_fanwei);
        fushenText = rootView.findViewById(R.id.fushen_fanwei);
        
        // 设置方位文字初始显示
        if (caishenText != null) caishenText.setText("正在获取...");
        if (xishenText != null) xishenText.setText("正在获取...");
        if (fushenText != null) fushenText.setText("正在获取...");
        
        dateView = rootView.findViewById(R.id.dateTime);
        compassDegreeText = rootView.findViewById(R.id.compass_degree);
        
        // 设置指南针初始文字
        if (compassDegreeText != null) {
            compassDegreeText.setText("初始化中...");
        }
        
        lunarDateText = rootView.findViewById(R.id.lunar_date);
        shengxiaoText = rootView.findViewById(R.id.shengxiao);
        jieqiText = rootView.findViewById(R.id.jieqi);
        chongshaText = rootView.findViewById(R.id.chongsha);
        wuxingText = rootView.findViewById(R.id.wuxing);
        shichenText = rootView.findViewById(R.id.shichen);
        personalLuckText = rootView.findViewById(R.id.personal_luck); // 初始化个人运势显示
        detailedFortuneText = rootView.findViewById(R.id.detailed_fortune); // 初始化详细运势显示
        loadingProgress = rootView.findViewById(R.id.loading_progress);
        datePickerButton = rootView.findViewById(R.id.date_picker_button);
        shareButton = rootView.findViewById(R.id.share_button);
        personalSettingButton = rootView.findViewById(R.id.personal_setting_button);
        
        // 初始化选中日期为今天
        selectedDate = Calendar.getInstance();
        
        // 设置日期选择按钮点击事件
        if (datePickerButton != null) {
            datePickerButton.setOnClickListener(v -> showDatePicker());
        }
        
        // 设置分享按钮点击事件
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> shareHuangliInfo());
        }
        
        // 设置个人设置按钮点击事件
        if (personalSettingButton != null) {
            personalSettingButton.setOnClickListener(v -> showPersonalSettings());
        }
    }

    /**
     * 初始化传感器
     */
    private void initializeSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }
    
    /**
     * 启动传感器监听
     */
    private void startSensorListening() {
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            if (magneticField != null) {
                sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    new Thread(this::fetchAndSetData).start();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置日期选择范围（可选：限制在过去和未来一年内）
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -1);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private void fetchAndSetData() {
        // 显示加载状态
        runOnUiThread(() -> {
            if (loadingProgress != null) {
                loadingProgress.setVisibility(View.VISIBLE);
            }
        });
        
        try {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH) + 1;
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            // 使用LunarHelper本地计算，无需网络请求
            JSONObject result = LunarHelper.getHuangLiData(year, month, day);
            
            if (result != null) {
                // 获取方位信息
                caishenDegrees = getDirectionInDegrees(result.getString("caishen"));
                xishenDegrees = getDirectionInDegrees(result.getString("xishen"));
                fushenDegrees = getDirectionInDegrees(result.getString("fushen"));

                final String yi = result.getString("yi")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .replace(",", "  ");
                final String ji = result.getString("ji")
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .replace(",", "  ");
                String dateFromApi = result.getString("yangli");
                
                // 如果不是今天，显示日期提示
                Calendar today = Calendar.getInstance();
                boolean isToday = (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                  selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                  selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
                
                final String displayDate;
                if (!isToday) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
                    displayDate = sdf.format(selectedDate.getTime());
                } else {
                    displayDate = dateFromApi;
                }
                
                final String cf = result.getString("caishen");
                final String xf = result.getString("xishen");
                final String ff = result.getString("fushen");
                
                // 获取更多信息
                final String lunar = result.optString("nongli", "");
                final String shengxiao = result.optString("shengxiao", "");
                final String star = result.optString("star", "");
                final String jieqiInfo = result.optString("jieqi", "");
                final String chongsha = result.optString("chong", "") + " " + result.optString("sha", "");
                final String suisha = result.optString("suisha", "");
                final String jiri = result.optString("jiri", "");
                
                // 获取五行、时辰信息
                final String wuxing = getWuxingInfoFromLunar(result);
                final String shichen = LunarHelper.getCurrentShiChenInfo();
                final String personalLuck = getPersonalLuckInfo(selectedDate);
                final String detailedFortune = getTodayBirthCompatibility();

                runOnUiThread(() -> {
                    // 恢复指针透明度表示加载完成
                    if (caishenPointer != null) caishenPointer.setAlpha(1.0f);
                    if (xishenPointer != null) xishenPointer.setAlpha(1.0f);
                    if (fushenPointer != null) fushenPointer.setAlpha(1.0f);
                    
                    caishenText.setText(cf);
                    xishenText.setText(xf);
                    fushenText.setText(ff);
                    yiTextView.setText(yi);
                    jiTextView.setText(ji);
                    dateView.setText(displayDate);
                    
                    // 更新新信息
                    if (lunarDateText != null && !lunar.isEmpty()) {
                        lunarDateText.setText("农历：" + lunar);
                        lunarDateText.setVisibility(View.VISIBLE);
                    }
                    if (shengxiaoText != null && !shengxiao.isEmpty()) {
                        shengxiaoText.setText("生肖：" + shengxiao);
                        shengxiaoText.setVisibility(View.VISIBLE);
                    }
                    if (jieqiText != null) {
                        // 优先显示节气信息，然后是星座和吉日信息
                        StringBuilder jieqiDisplay = new StringBuilder();
                        
                        if (!jieqiInfo.isEmpty()) {
                            jieqiDisplay.append(jieqiInfo);
                        }
                        
                        if (!star.isEmpty()) {
                            if (jieqiDisplay.length() > 0) jieqiDisplay.append(" | ");
                            jieqiDisplay.append("星座：").append(star);
                        }
                        
                        if (!jiri.isEmpty() && !jiri.equals("null")) {
                            if (jieqiDisplay.length() > 0) jieqiDisplay.append(" | ");
                            jieqiDisplay.append(jiri);
                        }
                        
                        if (jieqiDisplay.length() > 0) {
                            jieqiText.setText(jieqiDisplay.toString());
                            jieqiText.setVisibility(View.VISIBLE);
                        } else {
                            jieqiText.setVisibility(View.GONE);
                        }
                    }
                    if (chongshaText != null) {
                        // 显示冲煞信息（包含解释说明，分行显示）
                        StringBuilder chongshaDisplay = new StringBuilder();
                        
                        if (!chongsha.trim().isEmpty()) {
                            chongshaDisplay.append(chongsha.trim());
                        }
                        
                        if (!suisha.isEmpty()) {
                            if (chongshaDisplay.length() > 0) chongshaDisplay.append("\n");
                            chongshaDisplay.append(suisha);
                        }
                        
                        if (chongshaDisplay.length() > 0) {
                            chongshaText.setText(chongshaDisplay.toString());
                            chongshaText.setVisibility(View.VISIBLE);
                        } else {
                            chongshaText.setVisibility(View.GONE);
                        }
                    }
                    
                    // 显示五行信息
                    if (wuxingText != null && !wuxing.isEmpty()) {
                        wuxingText.setText(wuxing);
                        wuxingText.setVisibility(View.VISIBLE);
                    }
                    
                    // 显示时辰信息
                    if (shichenText != null && !shichen.isEmpty()) {
                        shichenText.setText(shichen);
                        shichenText.setVisibility(View.VISIBLE);
                    }
                    
                    // 显示个人运势
                    if (personalLuck != null && !personalLuck.isEmpty()) {
                        if (personalLuckText != null) {
                            personalLuckText.setText(personalLuck);
                            personalLuckText.setVisibility(View.VISIBLE);
                        }
                        
                        // 显示详细运势分析
                        if (detailedFortuneText != null && !detailedFortune.isEmpty()) {
                            detailedFortuneText.setText(detailedFortune);
                            detailedFortuneText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (personalLuckText != null) {
                            personalLuckText.setText("设置个人生辰信息后将显示个性化运势分析");
                            personalLuckText.setVisibility(View.VISIBLE);
                        }
                        if (detailedFortuneText != null) {
                            detailedFortuneText.setVisibility(View.GONE);
                        }
                    }
                    
                    // 隐藏加载状态
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                });
            } else {
                // 数据获取失败（理论上不会发生，因为是本地计算）
                runOnUiThread(() -> {
                    if (loadingProgress != null) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                    dateView.setText("黄历数据计算失败");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
                // 显示错误提示
                dateView.setText("黄历计算出错：" + e.getMessage());
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保传感器监听处于激活状态
        startSensorListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        
        // 如果两个传感器都就绪，则计算方向
        if (lastAccelerometerSet && lastMagnetometerSet) {
            // 控制更新频率
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= AppConstants.UPDATE_INTERVAL) {
                lastUpdateTime = currentTime;
                calculateOrientation();
            }
        }
    }

    /**
     * 计算设备方向
     */
    private void calculateOrientation() {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            
            // 获取方位角（弧度）
            float azimuthInRadians = orientation[0];
            
            // 转换为度数（0-360）
            float newAzimuth = (float) Math.toDegrees(azimuthInRadians);
            newAzimuth = CompassUtils.normalizeAngle(newAzimuth);
            
            // 快速响应初始值
            if (currentAzimuth < 0) {
                currentAzimuth = newAzimuth;
                lastValidAzimuth = newAzimuth;
            } else {
                // 计算角度差
                float diff = CompassUtils.calculateShortestRotationDiff(newAzimuth, currentAzimuth);
                
                // 精度过滤：只有当差值超过阈值时才更新
                if (CompassUtils.exceedsPrecisionThreshold(diff)) {
                    // 应用平滑滤波
                    float smoothedDiff = diff * (1 - AppConstants.ALPHA);
                    currentAzimuth = CompassUtils.normalizeAngle(currentAzimuth + smoothedDiff);
                    lastValidAzimuth = currentAzimuth;
                } else {
                    // 小幅度变化时使用上次有效值，减少抖动
                    currentAzimuth = lastValidAzimuth;
                }
            }
            
            // 更新度数显示
            if (compassDegreeText != null) {
                String direction = CompassUtils.getCompassDirection(currentAzimuth);
                runOnUiThread(() -> compassDegreeText.setText(String.format("%s %.0f°", direction, currentAzimuth)));
            }
            
            updatePointerRotation();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 传感器精度改变时的回调，无需特别处理
    }

    /**
     * 更新指针旋转
     */

    private void updatePointerRotation() {
        animatePointer(caishenPointer, caishenDegrees);
        animatePointer(xishenPointer, xishenDegrees);
        animatePointer(fushenPointer, fushenDegrees);
    }

    private float getDirectionInDegrees(String direction) {
        switch (direction) {
            case "东北": return 45f;
            case "正东": return 90f;
            case "东南": return 135f;
            case "正南": return 180f;
            case "西南": return 225f;
            case "正西": return 270f;
            case "西北": return 315f;
            case "正北": return 0f;
            default: return 0f;
        }
    }

    private String getCompassDirection(float azimuth) {
        if (azimuth >= 337.5 || azimuth < 22.5) return "北";
        if (azimuth >= 22.5 && azimuth < 67.5) return "东北";
        if (azimuth >= 67.5 && azimuth < 112.5) return "东";
        if (azimuth >= 112.5 && azimuth < 157.5) return "东南";
        if (azimuth >= 157.5 && azimuth < 202.5) return "南";
        if (azimuth >= 202.5 && azimuth < 247.5) return "西南";
        if (azimuth >= 247.5 && azimuth < 292.5) return "西";
        if (azimuth >= 292.5 && azimuth < 337.5) return "西北";
        return "";
    }
    
    private void animatePointer(ImageView pointer, float targetAzimuth) {
        // 计算指针应该指向的角度（相对于设备当前朝向）
        float targetRotation = (targetAzimuth - currentAzimuth + 360) % 360;
        
        // 获取当前旋转角度
        float currentRotation = pointer.getRotation() % 360;
        if (currentRotation < 0) currentRotation += 360;
        
        // 计算最短旋转路径
        float diff = targetRotation - currentRotation;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        
        // 只有当变化超过阈值时才执行动画，减少无效的微小旋转
        if (Math.abs(diff) > COMPASS_PRECISION_THRESHOLD) {
            float newRotation = currentRotation + diff;
            
            // 使用更长的动画时间和平滑插值器提高稳定性
            pointer.animate()
                    .rotation(newRotation)
                    .setDuration(100)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    // 获取五行信息（使用lunar-java库）
    private String getWuxingInfoFromLunar(JSONObject result) {
        try {
            String nayin = result.optString("nayin", "");
            String wuxing = result.optString("wuxing", "");
            
            StringBuilder wuxingInfo = new StringBuilder();
            
            if (!wuxing.isEmpty()) {
                wuxingInfo.append("今日五行：").append(wuxing);
                // 添加五行解释
                String wuxingExplanation = getWuxingExplanation(wuxing);
                if (!wuxingExplanation.isEmpty()) {
                    wuxingInfo.append("\n").append(wuxingExplanation);
                }
            }
            
            if (!nayin.isEmpty()) {
                if (wuxingInfo.length() > 0) {
                    wuxingInfo.append("\n\n");
                }
                wuxingInfo.append("纳音：").append(nayin);
                // 添加纳音解释
                String nayinExplanation = getNayinExplanation(nayin);
                if (!nayinExplanation.isEmpty()) {
                    wuxingInfo.append("\n").append(nayinExplanation);
                }
            }
            
            return wuxingInfo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    // 获取时辰吉凶信息
    private String getShichenInfo() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        
        String[] shichen = {"子时", "丑时", "寅时", "卯时", "辰时", "巳时", 
                           "午时", "未时", "申时", "酉时", "戌时", "亥时"};
        String[] shichenTime = {"23-01", "01-03", "03-05", "05-07", "07-09", "09-11",
                               "11-13", "13-15", "15-17", "17-19", "19-21", "21-23"};
        String[] jixiong = {"吉", "凶", "吉", "吉", "凶", "吉", "凶", "吉", "吉", "凶", "吉", "凶"};
        String[] wuxingShichen = {"水", "土", "木", "木", "土", "火", "火", "土", "金", "金", "土", "水"};
        String[] shichenExplanation = {
            "子时主静，宜休息思考，不宜剧烈活动",
            "丑时易困顿，宜静养，避免重要决策", 
            "寅时阳气始发，宜早起锻炼，精神饱满",
            "卯时朝阳东升，宜工作学习，效率较高",
            "辰时容易心浮气躁，宜保持冷静",
            "巳时阳气旺盛，宜处理重要事务",
            "午时阳气极盛易急躁，宜午休调节",
            "未时宜养神蓄力，为下午做准备",
            "申时精神焕发，宜创作思考",
            "酉时宜收心养性，准备休息",
            "戌时易烦躁不安，宜静心修养",
            "亥时宜早睡，为明日蓄养精神"
        };
        
        int index = (hour + 1) / 2 % 12;
        
        StringBuilder info = new StringBuilder();
        info.append("当前").append(shichen[index]).append("(").append(shichenTime[index]).append("点) - ").append(jixiong[index]);
        info.append("\n五行属").append(wuxingShichen[index]).append("，").append(shichenExplanation[index]);
        
        return info.toString();
    }
    
    // 获取五行解释
    private String getWuxingExplanation(String wuxing) {
        switch (wuxing) {
            case "金":
                return "金主坚韧刚毅，宜决断行事，但忌过刚易折";
            case "木":
                return "木主生长发达，宜开拓创新，生机勃勃";
            case "水":
                return "水主智慧流动，宜变通适应，柔能克刚";
            case "火":
                return "火主热情光明，宜积极进取，但忌急躁冒进";
            case "土":
                return "土主稳重厚德，宜踏实务实，守成为上";
            default:
                return "";
        }
    }
    
    // 获取纳音解释
    private String getNayinExplanation(String nayin) {
        if (nayin.contains("金")) {
            return "纳音属金，主坚固持久，利于长远规划";
        } else if (nayin.contains("木")) {
            return "纳音属木，主生发向上，利于事业发展";
        } else if (nayin.contains("水")) {
            return "纳音属水，主智慧灵活，利于学习思考";
        } else if (nayin.contains("火")) {
            return "纳音属火，主光明热情，利于人际交往";
        } else if (nayin.contains("土")) {
            return "纳音属土，主稳重踏实，利于基础建设";
        }
        return "纳音蕴含深意，宜顺势而为";
    }
    
    // 分享黄历信息
    private void shareHuangliInfo() {
        shareAsScreenshot();
    }
    
    // 文字分享
    private void shareAsText() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("📅 今日黄历信息\n\n");
        
        if (dateView != null) {
            shareText.append("日期：").append(dateView.getText()).append("\n");
        }
        
        if (lunarDateText != null && lunarDateText.getVisibility() == View.VISIBLE) {
            shareText.append(lunarDateText.getText()).append("\n");
        }
        
        if (yiTextView != null) {
            shareText.append("✅ 宜：").append(yiTextView.getText()).append("\n\n");
        }
        
        if (jiTextView != null) {
            shareText.append("❌ 忌：").append(jiTextView.getText()).append("\n\n");
        }
        
        if (caishenText != null) {
            shareText.append("💰 财神：").append(caishenText.getText()).append("\n");
        }
        
        if (xishenText != null) {
            shareText.append("😊 喜神：").append(xishenText.getText()).append("\n");
        }
        
        if (fushenText != null) {
            shareText.append("🍀 福神：").append(fushenText.getText()).append("\n\n");
        }
        
        shareText.append("🧭 来自黄历指南APP");
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "分享黄历信息"));
    }
    
    // 截图分享
    private void shareAsScreenshot() {
        try {
            // 创建一个专门用于分享的视图
            View shareView = createShareView();
            
            // 生成截图
            Bitmap bitmap = createBitmapFromView(shareView);
            
            // 保存到临时文件
            File imageFile = saveBitmapToFile(bitmap);
            
            if (imageFile != null) {
                // 分享图片
                Uri imageUri = FileProvider.getUriForFile(this, 
                    getPackageName() + ".provider", imageFile);
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "分享黄历截图"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果截图失败，降级到文字分享
            shareAsText();
        }
    }
    
    // 创建分享用的视图
    private View createShareView() {
        // 创建一个垂直线性布局作为分享内容容器
        LinearLayout shareLayout = new LinearLayout(this);
        shareLayout.setOrientation(LinearLayout.VERTICAL);
        shareLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        shareLayout.setPadding(50, 50, 50, 50);
        
        // 简洁的标题
        TextView titleView = new TextView(this);
        titleView.setText("电子黄历");
        titleView.setTextSize(24);
        titleView.setTextColor(Color.parseColor("#2C3E50"));
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 25);
        shareLayout.addView(titleView);
        
        // 日期信息卡片
        if (dateView != null && dateView.getText().length() > 0) {
            LinearLayout dateCard = createFormalInfoCard("日期", dateView.getText().toString(), "#34495E");
            shareLayout.addView(dateCard);
        }
        
        // 农历信息卡片
        if (lunarDateText != null && lunarDateText.getVisibility() == View.VISIBLE) {
            LinearLayout lunarCard = createFormalInfoCard("农历", lunarDateText.getText().toString().replace("农历：", ""), "#7B68EE");
            shareLayout.addView(lunarCard);
        }
        
        // 三神位置信息
        LinearLayout godsContainer = new LinearLayout(this);
        godsContainer.setOrientation(LinearLayout.HORIZONTAL);
        godsContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams godsParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        godsParams.setMargins(0, 15, 0, 15);
        godsContainer.setLayoutParams(godsParams);
        
        if (caishenText != null && caishenText.getText().length() > 0) {
            LinearLayout caishenCard = createFormalSmallCard("财神方位", caishenText.getText().toString(), "#E67E22");
            godsContainer.addView(caishenCard);
        }
        
        if (xishenText != null && xishenText.getText().length() > 0) {
            LinearLayout xishenCard = createFormalSmallCard("喜神方位", xishenText.getText().toString(), "#8E44AD");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            params.setMargins(15, 0, 15, 0);
            xishenCard.setLayoutParams(params);
            godsContainer.addView(xishenCard);
        }
        
        if (fushenText != null && fushenText.getText().length() > 0) {
            LinearLayout fushenCard = createFormalSmallCard("福神方位", fushenText.getText().toString(), "#27AE60");
            godsContainer.addView(fushenCard);
        }
        
        shareLayout.addView(godsContainer);
        
        // 分隔线
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dividerParams.setMargins(20, 25, 20, 25);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.parseColor("#BDC3C7"));
        shareLayout.addView(divider);
        
        // 今日宜忌
        if (yiTextView != null && yiTextView.getText().length() > 0) {
            LinearLayout yiCard = createFormalYiJiCard("今日宜", yiTextView.getText().toString(), "#27AE60", "#F0F8EC");
            shareLayout.addView(yiCard);
        }
        
        if (jiTextView != null && jiTextView.getText().length() > 0) {
            LinearLayout jiCard = createFormalYiJiCard("今日忌", jiTextView.getText().toString(), "#E67E22", "#FDF8F0");
            shareLayout.addView(jiCard);
        }
        
        // 个人运势信息
        if (personalLuckText != null && personalLuckText.getVisibility() == View.VISIBLE &&
            !personalLuckText.getText().toString().contains("设置个人生辰")) {
            
            // 分隔线
            View personalDivider = new View(this);
            LinearLayout.LayoutParams personalDividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
            personalDividerParams.setMargins(20, 20, 20, 20);
            personalDivider.setLayoutParams(personalDividerParams);
            personalDivider.setBackgroundColor(Color.parseColor("#BDC3C7"));
            shareLayout.addView(personalDivider);
            
            // 个人运势卡片
            LinearLayout personalCard = createFormalYiJiCard("个人运势", personalLuckText.getText().toString(), "#3498DB", "#EBF4FB");
            shareLayout.addView(personalCard);
        }
        
        // 底部标识
        TextView footerView = new TextView(this);
        footerView.setText("电子黄历 · trah开发");
        footerView.setTextSize(11);
        footerView.setTextColor(Color.parseColor("#95A5A6"));
        footerView.setGravity(Gravity.CENTER);
        footerView.setPadding(0, 30, 0, 10);
        shareLayout.addView(footerView);
        
        // 设置布局参数
        shareLayout.setLayoutParams(new ViewGroup.LayoutParams(
            900, ViewGroup.LayoutParams.WRAP_CONTENT));
            
        return shareLayout;
    }
    
    // 从视图创建位图
    private Bitmap createBitmapFromView(View view) {
        // 测量和布局视图
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        
        // 创建位图
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), 
            view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        
        return bitmap;
    }
    
    // 保存位图到文件
    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            // 创建缓存目录下的文件
            File cacheDir = new File(getCacheDir(), "share_images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            String fileName = "huangli_share_" + System.currentTimeMillis() + ".png";
            File imageFile = new File(cacheDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 显示个人设置对话框
    private void showPersonalSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("个人生辰信息设置");
        
        // 创建自定义布局
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        // 添加说明文本
        TextView infoText = new TextView(this);
        infoText.setText("设置您的出生信息，获取个性化运势分析");
        infoText.setPadding(0, 0, 0, 20);
        layout.addView(infoText);
        
        // 生日选择
        TextView birthLabel = new TextView(this);
        birthLabel.setText("选择生日：");
        layout.addView(birthLabel);
        
        Button birthDateButton = new Button(this);
        birthDateButton.setText(birthDate != null ?
                new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(birthDate.getTime()) : 
                "选择生日");
        birthDateButton.setOnClickListener(v -> showBirthDatePicker(birthDateButton));
        layout.addView(birthDateButton);
        
        // 时辰选择
        TextView timeLabel = new TextView(this);
        timeLabel.setText("选择出生时辰：");
        timeLabel.setPadding(0, 20, 0, 0);
        layout.addView(timeLabel);
        
        Spinner timeSpinner = new Spinner(this);
        String[] times = {"子时(23-01)", "丑时(01-03)", "寅时(03-05)", "卯时(05-07)", 
                         "辰时(07-09)", "巳时(09-11)", "午时(11-13)", "未时(13-15)",
                         "申时(15-17)", "酉时(17-19)", "戌时(19-21)", "亥时(21-23)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
        
        // 设置当前选中的时辰
        int currentTimeIndex = getCurrentBirthTimeIndex(birthTime);
        if (currentTimeIndex >= 0) {
            timeSpinner.setSelection(currentTimeIndex);
        }
        
        layout.addView(timeSpinner);
        
        // 显示当前设置的生辰运势提示
        TextView currentInfoText = new TextView(this);
        currentInfoText.setText("当前设置：" + (birthDate != null ? 
                new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(birthDate.getTime()) : "未设置生日") + 
                " " + birthTime);
        currentInfoText.setPadding(0, 20, 0, 0);
        currentInfoText.setTextColor(0xFF666666);
        layout.addView(currentInfoText);
        
        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 获取选中的时辰
            int selectedIndex = timeSpinner.getSelectedItemPosition();
            birthTime = times[selectedIndex];
            // 保存到偏好设置
            saveBirthInfo();
            // 重新计算运势并更新界面
            new Thread(this::fetchAndSetData).start();
        });
        builder.setNegativeButton("取消", null);
        
        builder.show();
    }
    
    // 获取当前生辰时间的索引
    private int getCurrentBirthTimeIndex(String timeString) {
        String[] times = {"子时(23-01)", "丑时(01-03)", "寅时(03-05)", "卯时(05-07)", 
                         "辰时(07-09)", "巳时(09-11)", "午时(11-13)", "未时(13-15)",
                         "申时(15-17)", "酉时(17-19)", "戌时(19-21)", "亥时(21-23)"};
        
        // 兼容旧版本的时辰格式（仅时辰名称）
        String[] simpleShichen = {"子时", "丑时", "寅时", "卯时", "辰时", "巳时", 
                                 "午时", "未时", "申时", "酉时", "戌时", "亥时"};
        
        // 首先尝试匹配完整格式
        for (int i = 0; i < times.length; i++) {
            if (times[i].equals(timeString)) {
                return i;
            }
        }
        
        // 然后尝试匹配简单格式
        for (int i = 0; i < simpleShichen.length; i++) {
            if (simpleShichen[i].equals(timeString)) {
                return i;
            }
        }
        
        return 0; // 默认返回子时
    }
    
    // 保存生辰信息
    private void saveBirthInfo() {
        if (birthDate != null) {
            SharedPreferences prefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("birth_date", birthDate.getTimeInMillis());
            editor.putString("birth_time", birthTime);
            editor.apply();
        }
    }
    
    /**
     * 加载个人生辰信息
     */
    private void loadBirthInfo() {
        birthInfo = PersonalInfoUtils.loadBirthInfo(this);
    }
    
    // 显示生日选择器
    private void showBirthDatePicker(Button button) {
        Calendar cal = birthDate != null ? birthDate : Calendar.getInstance();
        if (birthDate == null) {
            cal.add(Calendar.YEAR, -25); // 默认25岁
        }
        
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    birthDate = Calendar.getInstance();
                    birthDate.set(Calendar.YEAR, year);
                    birthDate.set(Calendar.MONTH, month);
                    birthDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
                    button.setText(sdf.format(birthDate.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        
        // 设置日期范围（100年前到今天）
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        picker.getDatePicker().setMinDate(minDate.getTimeInMillis());
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        picker.show();
    }
    
    // 获取个人运势信息
    private String getPersonalLuckInfo(Calendar date) {
        if (birthDate == null) {
            return "";
        }
        
        try {
            // 简单的生肖配对逻辑
            int birthYear = birthDate.get(Calendar.YEAR);
            int currentYear = date.get(Calendar.YEAR);
            
            String[] zodiac = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
            String birthZodiac = zodiac[(birthYear - 4) % 12];
            String currentZodiac = zodiac[(currentYear - 4) % 12];
            
            // 计算生肖相配情况
            String luckLevel = calculateZodiacLuck(birthZodiac, currentZodiac);
            
            // 获取简化的时辰名称
            String simpleTime = birthTime.contains("(") ? birthTime.substring(0, birthTime.indexOf("(")) : birthTime;
            
            return String.format("个人运势：%s年生肖%s，%s出生，今日运势%s", 
                    birthYear, birthZodiac, simpleTime, luckLevel);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    // 计算生肖运势
    private String calculateZodiacLuck(String birth, String current) {
        // 简化的生肖相配逻辑
        if (birth.equals(current)) {
            return "本命年，需谨慎";
        }
        
        // 六合生肖
        String[][] liuhe = {{"鼠", "牛"}, {"虎", "猪"}, {"兔", "狗"}, {"龙", "鸡"}, {"蛇", "猴"}, {"马", "羊"}};
        for (String[] pair : liuhe) {
            if ((birth.equals(pair[0]) && current.equals(pair[1])) || 
                (birth.equals(pair[1]) && current.equals(pair[0]))) {
                return "大吉";
            }
        }
        
        // 三合生肖
        String[][] sanhe = {{"鼠", "龙", "猴"}, {"牛", "蛇", "鸡"}, {"虎", "马", "狗"}, {"兔", "羊", "猪"}};
        for (String[] group : sanhe) {
            for (String animal : group) {
                if (birth.equals(animal)) {
                    for (String other : group) {
                        if (!other.equals(animal) && current.equals(other)) {
                            return "中吉";
                        }
                    }
                }
            }
        }
        
        // 六冲生肖
        String[][] liuchong = {{"鼠", "马"}, {"牛", "羊"}, {"虎", "猴"}, {"兔", "鸡"}, {"龙", "狗"}, {"蛇", "猪"}};
        for (String[] pair : liuchong) {
            if ((birth.equals(pair[0]) && current.equals(pair[1])) || 
                (birth.equals(pair[1]) && current.equals(pair[0]))) {
                return "需注意";
            }
        }
        
        return "平常";
    }
    
    // 获取今日与生辰合适度分析
    private String getTodayBirthCompatibility() {
        if (birthDate == null) {
            return "请先设置个人生辰信息以获取更准确的运势分析";
        }
        
        try {
            Calendar today = Calendar.getInstance();
            int birthMonth = birthDate.get(Calendar.MONTH) + 1;
            int birthDay = birthDate.get(Calendar.DAY_OF_MONTH);
            int todayMonth = today.get(Calendar.MONTH) + 1;
            int todayDay = today.get(Calendar.DAY_OF_MONTH);
            
            // 生肖运势分析
            String zodiacLuck = getPersonalLuckInfo(today);
            
            // 五行相配分析
            String wuxingAnalysis = getWuxingCompatibility(birthMonth, todayMonth);
            
            // 生日月份相配分析
            String monthCompatibility = getMonthCompatibility(birthMonth, todayMonth);
            
            // 数字吉凶分析（基于生日数字）
            String numberLuck = getNumberLuck(birthDay, todayDay);
            
            StringBuilder result = new StringBuilder("📊 今日与您的生辰合适度分析：\n\n");
            
            if (!zodiacLuck.isEmpty()) {
                result.append("🐲 ").append(zodiacLuck).append("\n");
            }
            
            if (!wuxingAnalysis.isEmpty()) {
                result.append("⚖️ ").append(wuxingAnalysis).append("\n");
            }
            
            if (!monthCompatibility.isEmpty()) {
                result.append("📅 ").append(monthCompatibility).append("\n");
            }
            
            if (!numberLuck.isEmpty()) {
                result.append("🔢 ").append(numberLuck).append("\n");
            }
            
            // 综合建议
            String overallAdvice = getOverallAdvice(birthMonth, todayMonth, birthDay, todayDay);
            if (!overallAdvice.isEmpty()) {
                result.append("\n💡 ").append(overallAdvice);
            }
            
            return result.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "运势分析暂时无法获取，请稍后再试";
        }
    }
    
    // 五行相配分析
    private String getWuxingCompatibility(int birthMonth, int todayMonth) {
        // 根据月份计算五行
        String[] monthWuxing = {"水", "水", "木", "木", "木", "火", "火", "火", "土", "金", "金", "水"};
        String birthWuxing = monthWuxing[birthMonth - 1];
        String todayWuxing = monthWuxing[todayMonth - 1];
        
        if (birthWuxing.equals(todayWuxing)) {
            return "五行相同（" + birthWuxing + "），今日能量与您共鸣，适合专注既定目标";
        }
        
        // 五行相生关系：水生木，木生火，火生土，土生金，金生水
        String[][] xiangsheng = {{"水", "木"}, {"木", "火"}, {"火", "土"}, {"土", "金"}, {"金", "水"}};
        
        for (String[] pair : xiangsheng) {
            if (birthWuxing.equals(pair[0]) && todayWuxing.equals(pair[1])) {
                return "五行相生（" + birthWuxing + "生" + todayWuxing + "），今日有利于发展和创新";
            }
            if (birthWuxing.equals(pair[1]) && todayWuxing.equals(pair[0])) {
                return "五行被生（" + todayWuxing + "生" + birthWuxing + "），今日容易得到帮助和支持";
            }
        }
        
        // 五行相克关系
        String[][] xiangke = {{"水", "火"}, {"火", "金"}, {"金", "木"}, {"木", "土"}, {"土", "水"}};
        
        for (String[] pair : xiangke) {
            if (birthWuxing.equals(pair[0]) && todayWuxing.equals(pair[1])) {
                return "五行相克（" + birthWuxing + "克" + todayWuxing + "），今日宜主动出击，但需控制力度";
            }
            if (birthWuxing.equals(pair[1]) && todayWuxing.equals(pair[0])) {
                return "五行被克（" + todayWuxing + "克" + birthWuxing + "），今日宜谨慎行事，避免冲突";
            }
        }
        
        return "五行关系平和，今日运势平稳";
    }
    
    // 月份相配分析
    private String getMonthCompatibility(int birthMonth, int todayMonth) {
        int diff = Math.abs(birthMonth - todayMonth);
        if (diff == 0) {
            return "今日正值您的出生月份，运势较旺，适合重要决策";
        } else if (diff == 6) {
            return "今日与您的出生月份相对，需要平衡处理各种关系";
        } else if (diff == 3 || diff == 9) {
            return "今日与您的出生月份成三角关系，利于创新和变化";
        } else if (diff == 1 || diff == 11) {
            return "今日与您的出生月份相邻，运势渐进，适合循序渐进";
        } else {
            return "今日月份能量与您调和，适合日常工作生活";
        }
    }
    
    // 数字吉凶分析
    private String getNumberLuck(int birthDay, int todayDay) {
        int birthSum = getNumberSum(birthDay);
        int todaySum = getNumberSum(todayDay);
        
        // 吉利数字：1,3,5,6,8,9
        int[] luckyNumbers = {1, 3, 5, 6, 8, 9};
        boolean birthLucky = false, todayLucky = false;
        
        for (int lucky : luckyNumbers) {
            if (birthSum == lucky) birthLucky = true;
            if (todaySum == lucky) todayLucky = true;
        }
        
        if (birthLucky && todayLucky) {
            return "数字能量吉利（" + birthSum + "&" + todaySum + "），今日运势颇佳";
        } else if (birthLucky || todayLucky) {
            return "数字能量尚可，今日运势平稳向上";
        } else if (birthSum == todaySum) {
            return "数字能量共鸣（" + birthSum + "），今日适合坚持本心";
        } else {
            return "数字能量平和，宜顺其自然";
        }
    }
    
    // 计算数字根（数字相加直到单位数）
    private int getNumberSum(int number) {
        while (number > 9) {
            number = (number / 10) + (number % 10);
        }
        return number;
    }
    
    // 综合建议
    private String getOverallAdvice(int birthMonth, int todayMonth, int birthDay, int todayDay) {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        
        String[] weekdays = {"", "日", "一", "二", "三", "四", "五", "六"};
        String todayWeek = weekdays[dayOfWeek];
        
        // 基于生辰和今日的综合建议
        if (Math.abs(birthMonth - todayMonth) <= 1) {
            return "今日与您的出生月份接近，星运相助，适合处理重要事务。建议在周" + todayWeek + "这天把握机会";
        } else if (birthDay % 2 == todayDay % 2) {
            return "今日与您的出生日期奇偶性相同，能量协调，适合团队协作和交流沟通";
        } else {
            return "今日运势平稳，建议保持积极心态，做好日常规划即可";
        }
    }
    
    // 创建信息卡片
    private LinearLayout createInfoCard(String icon, String title, String content, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(30, 20, 30, 20);
        card.setBackgroundColor(Color.WHITE);
        
        // 创建圆角效果
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12);
        drawable.setColor(Color.WHITE);
        drawable.setStroke(2, Color.parseColor("#E0E0E0"));
        card.setBackground(drawable);
        
        // 图标
        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(20);
        iconView.setPadding(0, 0, 15, 0);
        card.addView(iconView);
        
        // 内容容器
        LinearLayout contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(14);
        titleView.setTextColor(Color.parseColor(color));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        contentContainer.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(16);
        contentView.setTextColor(Color.parseColor("#333333"));
        contentView.setPadding(0, 5, 0, 0);
        contentContainer.addView(contentView);
        
        card.addView(contentContainer);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        card.setLayoutParams(params);
        
        return card;
    }
    
    // 创建小卡片
    private LinearLayout createSmallCard(String icon, String title, String content, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(15, 20, 15, 20);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12);
        drawable.setColor(Color.parseColor("#FAFAFA"));
        drawable.setStroke(2, Color.parseColor(color));
        card.setBackground(drawable);
        
        // 图标
        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(18);
        card.addView(iconView);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(12);
        titleView.setTextColor(Color.parseColor(color));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, 5, 0, 3);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(14);
        contentView.setTextColor(Color.parseColor("#333333"));
        card.addView(contentView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        card.setLayoutParams(params);
        
        return card;
    }
    
    // 创建宜忌卡片
    private LinearLayout createYiJiCard(String icon, String title, String content, String titleColor, String bgColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(25, 25, 25, 25);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12);
        drawable.setColor(Color.parseColor(bgColor));
        card.setBackground(drawable);
        
        // 标题容器
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setOrientation(LinearLayout.HORIZONTAL);
        titleContainer.setGravity(Gravity.CENTER);
        
        // 图标
        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(18);
        iconView.setPadding(0, 0, 10, 0);
        titleContainer.addView(iconView);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTextColor(Color.parseColor(titleColor));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleContainer.addView(titleView);
        
        card.addView(titleContainer);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(14);
        contentView.setTextColor(Color.parseColor("#333333"));
        contentView.setGravity(Gravity.CENTER);
        contentView.setPadding(0, 15, 0, 0);
        contentView.setLineSpacing(8, 1.0f);
        card.addView(contentView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);
        
        return card;
    }
    
    // 创建正式风格的信息卡片
    private LinearLayout createFormalInfoCard(String title, String content, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 15, 20, 15);
        card.setBackgroundColor(Color.parseColor("#F8F9FA"));
        
        // 创建简洁的边框
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(Color.parseColor("#F8F9FA"));
        drawable.setStroke(1, Color.parseColor("#E9ECEF"));
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(13);
        titleView.setTextColor(Color.parseColor(color));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(15);
        contentView.setTextColor(Color.parseColor("#2C3E50"));
        contentView.setPadding(0, 5, 0, 0);
        card.addView(contentView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        card.setLayoutParams(params);
        
        return card;
    }
    
    // 创建正式风格的小卡片
    private LinearLayout createFormalSmallCard(String title, String content, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(12, 15, 12, 15);
        
        // 创建简洁的背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(Color.parseColor("#FAFBFC"));
        drawable.setStroke(1, Color.parseColor(color));
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(11);
        titleView.setTextColor(Color.parseColor(color));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(13);
        contentView.setTextColor(Color.parseColor("#2C3E50"));
        contentView.setPadding(0, 3, 0, 0);
        card.addView(contentView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        card.setLayoutParams(params);
        
        return card;
    }
    
    // 创建正式风格的宜忌卡片
    private LinearLayout createFormalYiJiCard(String title, String content, String titleColor, String bgColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        
        // 创建简洁的背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(6);
        drawable.setColor(Color.parseColor(bgColor));
        card.setBackground(drawable);
        
        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(14);
        titleView.setTextColor(Color.parseColor(titleColor));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        card.addView(titleView);
        
        // 内容
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(13);
        contentView.setTextColor(Color.parseColor("#34495E"));
        contentView.setGravity(Gravity.CENTER);
        contentView.setPadding(0, 8, 0, 0);
        contentView.setLineSpacing(6, 1.0f);
        card.addView(contentView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        card.setLayoutParams(params);
        
        return card;
    }
    
    /**
     * 刷新黄历数据的公共方法，供HomeFragment调用
     */
    public void refreshHuangliData() {
        // 重置为今天的日期
        selectedDate = Calendar.getInstance();
        // 异步获取黄历数据
        new Thread(this::fetchAndSetData).start();
    }


}
