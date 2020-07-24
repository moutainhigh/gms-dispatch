package com.baseboot.interfaces.send;

import com.alibaba.fastjson.JSONObject;
import com.baseboot.common.service.DelayedService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.common.config.BaseConfig;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.global.Request;
import com.baseboot.common.service.MqService;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.input.InputCache;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CommSend {

    private final static long DELAY_TIME = 1000;

    /**
     * 发送心跳
     */
    public static void heartBeat(Integer vehicleId) {
        JSONObject json = new JSONObject();
        json.put("vehicleId", vehicleId);
        Request request = new Request().withMessage(json.toJSONString()).withRouteKey("VehHeartbeat2").withToWho(BaseConfig.REQUEST_COMM).withNeedPrint(false);
        MqService.request(request);
    }

    /**
     * 切换到自动模式
     */
    public static void vehModeAuto(Integer vehicleId) {
        log.debug("车辆[{}]发送切换【自动模式指令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehModeAuto").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 切换到远程模式
     */
    public static void vehModeRemote(Integer vehicleId) {
        log.debug("车辆[{}]发送切换【远程模式指令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehModeRemote").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 直线停车
     */
    public static void VehRemoteEmergencyParking(int vehicleId) {
        log.debug("车辆[{}]发送【紧急停车指令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehAutoEmergencyParking").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 启动车辆，完成自检并进入待机状态
     */
    public static void vehAutoStart(Integer vehicleId) {
        log.debug("车辆[{}]发送【车辆启动指令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehAutoStart").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 等待，进入待机状态
     */
    public static void vehAutoStandby(Integer vehicleId) {
        DelayedService.addTaskNoExist(() -> {
            vehAutoStandbyTimer(vehicleId);
        }, DELAY_TIME, TimerCommand.VEHICLE_AUTO_STANDBY_COMMAND + vehicleId, true).withNum(-1);
    }

    private static void vehAutoStandbyTimer(Integer vehicleId) {
        log.debug("车辆[{}]发送【待机指令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehAutoStandby").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 安全停车
     */
    public static void vehAutoSafeParking(Integer vehicleId) {
        DelayedService.addTaskNoExist(() -> {
            vehAutoSafeParkingTimer(vehicleId);
        }, DELAY_TIME, TimerCommand.VEHICLE_SAFE_STOP_COMMAND + vehicleId, true).withNum(5);
    }

    private static void vehAutoSafeParkingTimer(Integer vehicleId) {
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null != vehicleTask) {
            //只有在运行状态发送安全停车才有意义
            if (vehicleTask.isStart()) {
                InputCache.getDispatchInput(vehicleId).stopRun();
                log.debug("车辆[{}]发送【原路径安全停车指令】", vehicleId);
                Map<String, Object> params = new HashMap<>();
                params.put("vehicleId", vehicleId);
                Request request = new Request().withRouteKey("VehAutoSafeParking").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
                MqService.request(request);
            } else {
                BaseUtil.cancelDelayTask(TimerCommand.VEHICLE_SAFE_STOP_COMMAND + vehicleId);
            }
        }
    }

    /**
     * 轨迹跟随
     */
    public static void vehAutoTrailFollowing(Integer vehicleId, byte[] bytes) {
        log.debug("车辆[{}]发送【轨迹跟随指令】", vehicleId);
        Request request = new Request().withRouteKey("VehAutoTrailFollowing").withBytes(bytes).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 装载制动
     */
    public static void vehAutoLoadBrake(Integer vehicleId, Integer value) {
        log.debug("车辆[{}]发送【装载制动命令】", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        params.put("value", value);
        Request request = new Request().withRouteKey("VehAutoLoadBrake").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }


    /**
     * 卸矿
     */
    public static void vehAutoUnload(Integer vehicleId) {
        DelayedService.addTaskNoExist(() -> {
            vehAutoUnloadTimer(vehicleId);
        }, 2000, TimerCommand.VEHICLE_AUTO_UNLOAD_COMMAND + vehicleId, true).withNum(10);
    }

    private static void vehAutoUnloadTimer(Integer vehicleId) {
        log.debug("车辆[{}]发送卸矿命令", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehAutoUnload").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

    /**
     * 排土
     */
    public static void vehAutoDump(Integer vehicleId) {
        log.debug("车辆[{}]发送排土命令", vehicleId);
        Map<String, Object> params = new HashMap<>();
        params.put("vehicleId", vehicleId);
        Request request = new Request().withRouteKey("VehAutoDump").withMessage(BaseUtil.toJson(params)).withToWho(BaseConfig.REQUEST_COMM);
        MqService.request(request);
    }

}
