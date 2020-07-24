package com.baseboot.interfaces.send;

import com.baseboot.common.config.BaseConfig;
import com.baseboot.common.service.MqService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.global.Request;
import com.baseboot.entry.global.RetryMessage;
import com.baseboot.entry.map.Point;
import com.baseboot.interfaces.receive.MapReceive;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.DispatchUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MapSend {

    /**
     * 初始地图区域信息
     */
    public static void initMapAreaInfo(int mapId) {
        Request request = new Request();
        Map<String, Object> params = new HashMap<>();
        params.put("mapId", mapId);
        request.withToWho(BaseConfig.REQUEST_MAP).withMessage(BaseUtil.toJson(params)).withRouteKey("getSemiStaticLayerInfo");
        MqService.request(request);
    }

    /**
     * 获取路径
     * {@link MapReceive#getGlobalPath}
     */
    public static void getGlobalPath(VehicleTask vehicleTask, Integer vehicleId, Point startPoint, Point endPoint, Integer planType) {
        String messageId = TimerCommand.PATH_RETRY_COMMAND + vehicleId;
        RetryMessage message = BaseCacheUtil.getRetryMessage(messageId);
        if (null == message) {
            log.error("获取路径为非法请求!");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        Integer mapId = DispatchUtil.getActivateMapId();
        if (null == mapId) {
            log.error("活动地图id不存在");
            return;
        }
        params.put("mapId", mapId);
        params.put("vehicleId", vehicleId);
        params.put("planType", planType);
        params.put("begin", startPoint);
        params.put("end", endPoint);
        Request request = new Request()
                .withRouteKey("getGlobalPath")
                .withToWho(BaseConfig.REQUEST_MAP)
                .withMessage(BaseUtil.toJson(params))
                .withMessageId(messageId);
        message.withExecute(() -> {
            MqService.request(request);
        }).withRequest(request);
        message.setResult(() -> {
            boolean createError = vehicleTask.getHelper().getPathManager().pathCreateError();
            if (createError) {//生成路径失败
                message.fail();
            }
        }).start();
    }

    /**
     * 获取轨迹
     */
    public static void getTrajectory(Integer activateMapId, Integer vehicleId, double curSpeed, int startId, int endId) {
        log.debug("[{}]发送轨迹请求命令", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("mapId", activateMapId);
        params.put("vehicleId", vehicleId);
        params.put("nowSpeed", curSpeed);
        params.put("beginIdx", startId);
        params.put("endIdx", endId);
        Request request = new Request();
        request.withToWho(BaseConfig.RESPONSE_MAP).withRouteKey("getTrajectoryByIdx").withMessage(BaseUtil.toJson(params));
        MqService.request(request);
    }
}
