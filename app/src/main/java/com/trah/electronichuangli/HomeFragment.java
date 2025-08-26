package com.trah.electronichuangli;

// ==================== Android Framework 导入 ====================
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

// ==================== 项目内部导入 ====================
import com.trah.electronichuangli.constants.AppConstants;

/**
 * 首页Fragment
 * 显示主要的黄历信息和方位指针功能
 * 
 * @author trah
 * @version 1.0
 */
public class HomeFragment extends Fragment {
    
    // ==================== UI组件 ====================
    private View fragmentView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MainActivity mainActivity;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        return fragmentView;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 获取MainActivity实例
        if (getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }
        
        // 初始化下拉刷新
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        setupSwipeRefresh();
        
        // 调用MainActivity的初始化方法，并传递当前Fragment的View
        if (mainActivity != null) {
            mainActivity.initializeHomeFragment(fragmentView);
        }
    }
    
    /**
     * 设置下拉刷新功能
     */
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            // 设置下拉刷新的颜色
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            );
            
            // 设置下拉刷新监听器
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // 执行刷新操作
                refreshData();
            });
        }
    }
    
    /**
     * 刷新黄历数据
     */
    private void refreshData() {
        if (mainActivity != null) {
            // 在后台线程执行数据刷新
            new Thread(() -> {
                try {
                    // 模拟加载时间
                    Thread.sleep(AppConstants.REFRESH_DELAY);
                    
                    // 在主线程中执行数据获取
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // 重新获取黄历数据
                            mainActivity.refreshHuangliData();
                            // 停止刷新动画
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }).start();
        } else {
            // 如果没有MainActivity引用，直接停止刷新
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
    
    /**
     * 获取Fragment的根视图
     * @return Fragment视图
     */
    public View getFragmentView() {
        return fragmentView;
    }
}