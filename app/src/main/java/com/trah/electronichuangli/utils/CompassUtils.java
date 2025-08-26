package com.trah.electronichuangli.utils;

import com.trah.electronichuangli.constants.AppConstants;

/**
 * 指南针和方位相关工具类
 * 处理方位角度转换、方向判断等功能
 * 
 * @author trah  
 * @version 1.0
 */
public class CompassUtils {
    
    /**
     * 根据方位名称获取对应的度数
     * @param direction 方位名称（如"正北"、"东南"等）
     * @return 对应的度数值
     */
    public static float getDirectionInDegrees(String direction) {
        switch (direction) {
            case "东北": return AppConstants.Direction.NORTHEAST;
            case "正东": return AppConstants.Direction.EAST;
            case "东南": return AppConstants.Direction.SOUTHEAST;
            case "正南": return AppConstants.Direction.SOUTH;
            case "西南": return AppConstants.Direction.SOUTHWEST;
            case "正西": return AppConstants.Direction.WEST;
            case "西北": return AppConstants.Direction.NORTHWEST;
            case "正北": 
            default: 
                return AppConstants.Direction.NORTH;
        }
    }
    
    /**
     * 根据方位角度获取对应的方位名称
     * @param azimuth 方位角度（0-360度）
     * @return 方位名称（如"北"、"东南"等）
     */
    public static String getCompassDirection(float azimuth) {
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
    
    /**
     * 计算最短旋转路径的角度差
     * @param targetRotation 目标角度
     * @param currentRotation 当前角度
     * @return 最短路径的角度差
     */
    public static float calculateShortestRotationDiff(float targetRotation, float currentRotation) {
        float diff = targetRotation - currentRotation;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return diff;
    }
    
    /**
     * 规范化角度到0-360度范围内
     * @param angle 原始角度
     * @return 规范化后的角度（0-360度）
     */
    public static float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }
    
    /**
     * 检查角度变化是否超过精度阈值
     * @param diff 角度差值
     * @return 是否超过阈值
     */
    public static boolean exceedsPrecisionThreshold(float diff) {
        return Math.abs(diff) > AppConstants.COMPASS_PRECISION_THRESHOLD;
    }
}