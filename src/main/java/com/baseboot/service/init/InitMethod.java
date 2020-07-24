package com.baseboot.service.init;

import com.baseboot.common.utils.DateUtil;
import com.baseboot.entry.global.BaseConstant;
import com.baseboot.entry.global.RedisKeyPool;
import com.baseboot.interfaces.receive.MapReceive;
import com.baseboot.interfaces.send.MapSend;
import com.baseboot.service.DispatchUtil;
import com.baseboot.common.service.RedisService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitMethod {

    /**
     * 清理之前缓存的路径、轨迹
     * */
    public static void clearCache(){
        RedisService.delPattern(BaseConstant.MONITOR_DB,RedisKeyPool.VAP_PATH_PREFIX);
        RedisService.delPattern(BaseConstant.MONITOR_DB,RedisKeyPool.VAP_BASE_PREFIX);
        RedisService.delPattern(BaseConstant.MONITOR_DB,RedisKeyPool.VAP_TRAIL_PREFIX);
    }

    /**
     * 初始化车辆、调度单元
     * */
    public static void dispatchInit() {
        String time = DateUtil.formatLongToString(System.currentTimeMillis());
        RedisService.set(BaseConstant.MONITOR_DB, RedisKeyPool.DISPATCH_SERVER_INIT, time);
    }

    /**
     * 初始化地图任务区
     * {@link MapReceive#getSemiStaticLayerInfo}
     * */
    public static void mapInit() {
        Integer mapId = DispatchUtil.getActivateMapId();
        if(null==mapId){
            log.error("******************获取活动地图id失败,初始化任务区失败!******************");
            return;
        }
        MapSend.initMapAreaInfo(mapId);
    }
}
