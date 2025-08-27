package com.trah.electronichuangli.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 真随机数生成器
 * 结合用户滑动、传感器数据、系统时间等多种随机源
 * 提供实时随机数显示和收集功能
 * 
 * @author trah
 * @version 1.0
 */
public class TrueRandomGenerator implements SensorEventListener {
    
    // ==================== 随机源接口 ====================
    public interface RandomDataListener {
        void onRandomDataChanged(RandomData randomData);
        void onSeedCollected(long seed, String source);
        void onCollectionProgress(int collected, int target);
    }
    
    // ==================== 随机数据结构 ====================
    public static class RandomData {
        public long currentSeed;           // 当前随机种子
        public float touchX, touchY;       // 触摸坐标
        public float touchSpeed;           // 滑动速度
        public long systemNanoTime;        // 系统纳秒时间
        public float accelerometerX, accelerometerY, accelerometerZ; // 加速度计
        public float gyroscopeX, gyroscopeY, gyroscopeZ;            // 陀螺仪
        public float magneticX, magneticY, magneticZ;               // 磁场
        public String currentSource;       // 当前随机源
        public int collectedCount;         // 已收集数量
        public boolean isReady;            // 是否收集足够
        
        @Override
        public String toString() {
            return String.format("Seed: %d, Touch: (%.1f,%.1f), Speed: %.1f", 
                               currentSeed, touchX, touchY, touchSpeed);
        }
    }
    
    // ==================== 成员变量 ====================
    private final SecureRandom secureRandom;
    private final SensorManager sensorManager;
    private final List<Long> collectedSeeds;
    private final List<String> seedSources;
    private final RandomData currentRandomData;
    private RandomDataListener listener;
    
    // 传感器相关
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastGyroscope = new float[3];
    private float[] lastMagnetic = new float[3];
    
    // 触摸相关
    private float lastTouchX, lastTouchY;
    private long lastTouchTime;
    
    // 收集参数
    private static final int TARGET_SEED_COUNT = 30;  // 目标收集数量
    private static final long MIN_SEED_INTERVAL = 50; // 最小收集间隔(ms) - 降低间隔
    private long lastSeedTime = 0;
    // UI更新相关
    private Runnable uiUpdateRunnable;
    private static final long MIN_UPDATE_INTERVAL = 16; // 最小UI更新间隔(ms) - 60fps
    private boolean userInteracting = false; // 用户是否正在交互
    
    // ==================== 构造函数 ====================
    public TrueRandomGenerator(Context context) {
        this.secureRandom = new SecureRandom();
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.collectedSeeds = new ArrayList<>();
        this.seedSources = new ArrayList<>();
        this.currentRandomData = new RandomData();
        
        // 初始化传感器
        initializeSensors();
        
        // 设置初始随机种子
        updateCurrentSeed("初始化完成");
    }
    
    // ==================== 传感器初始化 ====================
    private void initializeSensors() {
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
    }
    
    // ==================== 监听器设置 ====================
    public void setRandomDataListener(RandomDataListener listener) {
        this.listener = listener;
    }
    
    // ==================== 传感器管理 ====================
    public void startSensorListening() {
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
            if (gyroscope != null) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
            }
            if (magnetometer != null) {
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }
    
    public void stopSensorListening() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    
    // ==================== 触摸事件处理 ====================
    public void processTouchEvent(MotionEvent event) {
        long currentTime = System.currentTimeMillis();
        
        // 处理所有触摸事件类型
        if (event.getAction() == MotionEvent.ACTION_DOWN || 
            event.getAction() == MotionEvent.ACTION_MOVE ||
            event.getAction() == MotionEvent.ACTION_UP) {
            
            float x = event.getX();
            float y = event.getY();
            
            // 设置用户交互状态
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                userInteracting = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                userInteracting = false;
            }
            
            // 计算滑动速度
            float speed = 0;
            if (lastTouchTime > 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
                float distance = (float) Math.sqrt(Math.pow(x - lastTouchX, 2) + Math.pow(y - lastTouchY, 2));
                float timeInterval = (currentTime - lastTouchTime) / 1000.0f; // 转为秒
                speed = timeInterval > 0 ? distance / timeInterval : 0;
            }
            
            // 更新触摸数据
            currentRandomData.touchX = x;
            currentRandomData.touchY = y;
            currentRandomData.touchSpeed = speed;
            
            // 记录上次触摸信息
            lastTouchX = x;
            lastTouchY = y;
            lastTouchTime = currentTime;
            
            // 只在用户交互时更新随机种子和UI
            if (userInteracting) {
                String source = String.format("用户滑动");
                updateCurrentSeed(source);
                
                // 收集种子
                collectSeed(source);
            }
        }
    }
    
    // ==================== 传感器事件处理 ====================
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 传感器数据只用于随机种子生成，不触发UI更新
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, lastAccelerometer, 0, 3);
                currentRandomData.accelerometerX = event.values[0];
                currentRandomData.accelerometerY = event.values[1];
                currentRandomData.accelerometerZ = event.values[2];
                break;
                
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, lastGyroscope, 0, 3);
                currentRandomData.gyroscopeX = event.values[0];
                currentRandomData.gyroscopeY = event.values[1];
                currentRandomData.gyroscopeZ = event.values[2];
                break;
                
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, lastMagnetic, 0, 3);
                currentRandomData.magneticX = event.values[0];
                currentRandomData.magneticY = event.values[1];
                currentRandomData.magneticZ = event.values[2];
                break;
        }
        // 传感器数据不主动更新种子，只作为随机性的输入源
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 传感器精度变化，可以忽略
    }
    
    // ==================== 随机种子生成和更新 ====================
    private void updateCurrentSeed(String source) {
        // 获取系统纳秒时间
        long nanoTime = System.nanoTime();
        currentRandomData.systemNanoTime = nanoTime;
        currentRandomData.currentSource = source;
        
        // 结合多种数据源生成随机种子
        long seed = generateCombinedSeed();
        currentRandomData.currentSeed = seed;
        
        // 更新收集状态
        currentRandomData.collectedCount = collectedSeeds.size();
        currentRandomData.isReady = collectedSeeds.size() >= TARGET_SEED_COUNT;
        
        // 通知监听器更新UI
        if (listener != null) {
            listener.onRandomDataChanged(currentRandomData);
        }
    }
    
    private long generateCombinedSeed() {
        // 使用多个随机源生成超大随机种子
        long seed1 = secureRandom.nextLong();
        long seed2 = secureRandom.nextLong();
        long seed3 = secureRandom.nextLong();
        
        // 系统时间相关
        long nanoTime = System.nanoTime();
        long currentTime = System.currentTimeMillis();
        
        // 触摸数据的复杂组合
        long touchSeed = 0;
        touchSeed ^= Double.doubleToLongBits(currentRandomData.touchX * currentRandomData.touchY);
        touchSeed ^= Double.doubleToLongBits(currentRandomData.touchSpeed * 1000000);
        touchSeed ^= Float.floatToIntBits(currentRandomData.touchX) * Float.floatToIntBits(currentRandomData.touchY);
        
        // 传感器数据的复杂混合
        long sensorSeed = 0;
        sensorSeed ^= Double.doubleToLongBits(currentRandomData.accelerometerX * currentRandomData.gyroscopeX);
        sensorSeed ^= Double.doubleToLongBits(currentRandomData.accelerometerY * currentRandomData.magneticY);
        sensorSeed ^= Double.doubleToLongBits(currentRandomData.accelerometerZ * currentRandomData.gyroscopeZ);
        sensorSeed ^= Float.floatToIntBits(currentRandomData.magneticX * currentRandomData.magneticY * currentRandomData.magneticZ);
        
        // 多层混合算法
        long finalSeed = seed1;
        finalSeed ^= Long.rotateLeft(seed2, 21);
        finalSeed ^= Long.rotateRight(seed3, 13);
        finalSeed ^= Long.rotateLeft(nanoTime, 7);
        finalSeed ^= Long.rotateRight(currentTime, 31);
        finalSeed ^= Long.rotateRight(touchSeed, 11);
        finalSeed ^= Long.rotateLeft(sensorSeed, 23);
        
        // 哈希混合增加随机性
        finalSeed ^= Long.reverseBytes(finalSeed);
        finalSeed *= 0x9E3779B97F4A7C15L; // 大质数乘法
        finalSeed ^= finalSeed >>> 32;
        finalSeed *= 0x85EBCA6B; 
        finalSeed ^= finalSeed >>> 16;
        
        // 额外的扰动
        for (int i = 0; i < 3; i++) {
            finalSeed ^= secureRandom.nextLong();
            finalSeed = Long.rotateLeft(finalSeed, (int)(finalSeed % 64));
        }
        
        return Math.abs(finalSeed); // 确保为正数
    }
    
    // ==================== 种子收集 ====================
    private void collectSeed(String source) {
        long currentTime = System.currentTimeMillis();
        
        // 控制收集频率，避免过于频繁
        if (currentTime - lastSeedTime >= MIN_SEED_INTERVAL && 
            collectedSeeds.size() < TARGET_SEED_COUNT) {
            
            long seed = currentRandomData.currentSeed;
            collectedSeeds.add(seed);
            seedSources.add(source);
            lastSeedTime = currentTime;
            
            if (listener != null) {
                listener.onSeedCollected(seed, source);
                listener.onCollectionProgress(collectedSeeds.size(), TARGET_SEED_COUNT);
            }
        }
    }
    
    // ==================== 公共接口 ====================
    
    /**
     * 获取当前随机数据
     */
    public RandomData getCurrentRandomData() {
        return currentRandomData;
    }
    
    /**
     * 检查是否收集了足够的随机数
     */
    public boolean isReady() {
        return collectedSeeds.size() >= TARGET_SEED_COUNT;
    }
    
    /**
     * 获取收集进度
     */
    public String getProgress() {
        return String.format("%d/%d", collectedSeeds.size(), TARGET_SEED_COUNT);
    }
    
    /**
     * 重置收集状态
     */
    public void reset() {
        collectedSeeds.clear();
        seedSources.clear();
        currentRandomData.collectedCount = 0;
        currentRandomData.isReady = false;
    }
    
    /**
     * 生成最终的卜卦用随机数组
     * @return 6个随机数，用于生成六爻
     */
    public int[] generateFinalRandomNumbers() {
        if (!isReady()) {
            throw new IllegalStateException("随机数收集未完成，无法生成最终随机数");
        }
        
        // 使用收集的种子初始化新的随机数生成器
        long finalSeed = 0;
        for (int i = 0; i < collectedSeeds.size(); i++) {
            finalSeed ^= collectedSeeds.get(i) << (i % 32);
        }
        
        SecureRandom finalRandom = new SecureRandom();
        finalRandom.setSeed(finalSeed);
        
        // 生成6个随机数用于六爻
        int[] results = new int[6];
        for (int i = 0; i < 6; i++) {
            // 生成0-3的随机数，用于传统的六爻算法
            results[i] = finalRandom.nextInt(4);
        }
        
        return results;
    }
    
    /**
     * 获取随机数收集的详细信息
     */
    public String getCollectionDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("随机数收集详情:\n");
        sb.append(String.format("已收集: %d/%d\n", collectedSeeds.size(), TARGET_SEED_COUNT));
        
        if (!collectedSeeds.isEmpty()) {
            sb.append("最近5个种子:\n");
            int start = Math.max(0, collectedSeeds.size() - 5);
            for (int i = start; i < collectedSeeds.size(); i++) {
                sb.append(String.format("  %d: %d (%s)\n", 
                    i + 1, collectedSeeds.get(i), seedSources.get(i)));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 手动添加随机源（测试用）
     */
    public void addManualRandomness() {
        String source = "手动添加";
        updateCurrentSeed(source);
        collectSeed(source);
    }
    
    /**
     * 强制更新UI显示（定期调用）
     */
    public void forceUpdateUI() {
        if (listener != null) {
            listener.onRandomDataChanged(currentRandomData);
        }
    }
}