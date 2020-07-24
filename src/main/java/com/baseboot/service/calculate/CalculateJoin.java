package com.baseboot.service.calculate;

import com.baseboot.common.utils.SpringContextUtil;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.service.BaseCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 异步计算获取最短终点路径编号
 */
@Slf4j
public class CalculateJoin {

    private static Set<Calculate> calculates = new HashSet<>();

    static {
        ApplicationContext context = SpringContextUtil.getContext();
        Map<String, Object> beans = context.getBeansWithAnnotation(CalculateClass.class);
        beans.values().forEach(bean -> {
            calculates.add((Calculate) bean);
        });
    }

    /**
     * 获取最短位置索引
     */
    public static int getJoinMinId(Integer vehicleId) {
        int index = 100000;
        for (Calculate calculate : calculates) {
            int endPoint = calculate.calculateEndPoint(vehicleId);
            index = index > endPoint ? endPoint : index;
        }
        WorkPathInfo workPathInfo = BaseCacheUtil.getWorkPathInfo(vehicleId);
        workPathInfo.setTrailEndId(index);//设置轨迹终点
        if (index == 100000)
            index = 0;
        return index;
    }
}
