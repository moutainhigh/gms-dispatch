package com.baseboot.interfaces.receive;

import com.baseboot.common.annotation.Parser;
import com.baseboot.common.config.BaseConfig;
import com.baseboot.common.service.MqService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.common.utils.DateUtil;
import com.baseboot.entry.dispatch.area.LoadArea;
import com.baseboot.entry.dispatch.area.LoadPoint;
import com.baseboot.entry.dispatch.area.UnLoadMineralArea;
import com.baseboot.entry.dispatch.monitor.vehicle.Unit;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.global.Response;
import com.baseboot.entry.map.Point;
import com.baseboot.enums.AreaTypeEnum;
import com.baseboot.enums.NotifyCommandEnum;
import com.baseboot.interfaces.send.CommSend;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.input.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class BusiReceive {

    private final static BusiReceive instance = new BusiReceive();

    public static void dispense(String message, String routeKey, String messageId) {
        log.debug("收到业务层消息,routeKey={}", routeKey);
        Response response = new Response().withRouteKey(routeKey).withMessageId(messageId).withToWho(BaseConfig.RESPONSE_BUSI);
        try {
            Method method = instance.getClass().getDeclaredMethod(BaseUtil.subIndexStr(routeKey, "."), String.class, Response.class);
            method.setAccessible(true);
            method.invoke(instance, message, response);
        } catch (NoSuchMethodException e) {
            log.error("BusiReceive has not [{}] method", routeKey, e);
        } catch (IllegalAccessException e) {
            log.error("BusiReceive access error [{}] method", routeKey, e);
        } catch (InvocationTargetException e) {
            log.error("BusiReceive call error [{}] method", routeKey, e);
        } finally {
            MqService.response(response);
        }
    }

    /**
     * 初始化车辆
     */
    @SuppressWarnings("unchecked")
    public void initVeh(String message, Response response) {
        HashMap<String, ArrayList> params = BaseUtil.toObj(message, HashMap.class);
        if (null != params && params.containsKey("vehicles")) {
            ArrayList<Integer> vehicleIds = BaseUtil.get(params, "vehicles", ArrayList.class);
            BaseCacheUtil.initVehicles(vehicleIds);
            response.withSucMessage("初始化车辆成功");
        } else {
            response.withFailMessage("初始化车辆失败");
        }
    }


    /**
     * 创建调度单元
     */
    public void createLoaderAIUnit(String message, Response response) {
        log.debug("创建调度单元,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Unit unit = BaseUtil.toObj(message, Unit.class);

        Integer loaderAreaId = BaseUtil.get(params, "loaderAreaId", Integer.class);
        LoadArea loadArea = BaseCacheUtil.getTaskArea(loaderAreaId);
        unit.setLoadArea(loadArea);
        Integer unLoaderAreaId = BaseUtil.get(params, "unLoaderAreaId", Integer.class);
        UnLoadMineralArea unLoadArea = BaseCacheUtil.getTaskArea(unLoaderAreaId);
        ;
        unit.setUnloadArea(unLoadArea);

        if (!Parser.notNull(unit)) {
            log.debug("创建调度单元参数异常!");
            response.withFailMessage("创建调度单元参数异常");
            return;
        }
        if (BaseCacheUtil.addUnit(unit)) {
            response.withSucMessage("创建调度单成功");
        } else {
            response.withFailMessage("该调度单元已存在");
        }
    }

    /**
     * 修改调度单元
     */
    public void modifyLoaderAIUnit(String message, Response response) {
        log.debug("修改调度单元,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer unitId = BaseUtil.get(params, "unitId", Integer.class);
        Integer cycleTimes = BaseUtil.get(params, "cycleTimes", Integer.class);
        String endTime = BaseUtil.get(params, "endTime", String.class);
        Unit unit = BaseCacheUtil.getUnit(unitId);
        if (null == unit) {
            log.debug("调度单元不存在,{}", unitId);
            response.withFailMessage("调度单元不存在");
            return;
        }
        if (null != cycleTimes && cycleTimes >= 0) {
            unit.setCycleTimes(cycleTimes);
        }
        if (BaseUtil.StringNotNull(endTime)) {
            Date time = DateUtil.formatStringTime(endTime, DateUtil.FULL_TIME_SPLIT_PATTERN);
            if (null != time && time.getTime() > BaseUtil.getCurTime()) {
                unit.setEndTime(time);
            }
        }
        unit.updateCache();
        response.withSucMessage("修改调度单元成功");
    }

    /**
     * 删除调度单元
     */
    public void removeAIUnit(String message, Response response) {
        log.debug("删除调度单元,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer unitId = BaseUtil.get(params, "unitId", Integer.class);
        Unit unit = BaseCacheUtil.getUnit(unitId);
        if (null == unit) {
            log.debug("调度单元不存在,{}", unitId);
            response.withFailMessage("调度单元不存在");
            return;
        }
        boolean notify = unit.delUnitNotify();
        if (notify) {
            response.withSucMessage("调度单元已删除");
        } else {
            response.withSucMessage("调度单元等待删除中");
        }
    }

    /**
     * 调度单元增加车辆
     */
    @SuppressWarnings("unchecked")
    public void addLoadAIVeh(String message, Response response) {
        log.debug("调度单元添加车辆,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer unitId = BaseUtil.get(params, "unitId", Integer.class);
        ArrayList<Integer> vehicleIds = BaseUtil.get(params, "vehicleIds", ArrayList.class);
        Unit unit = BaseCacheUtil.getUnit(unitId);
        if (null == unit) {
            log.debug("调度单元不存在,{}", unitId);
            response.withFailMessage("调度单元不存在");
            return;
        }
        for (Integer vehicleId : vehicleIds) {
            VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
            if (null == vehicleTask) {
                log.debug("系统中不存在该车辆信息,{}", unitId);
                continue;
            }
            unit.addVehicleTask(vehicleTask);
        }
        response.withSucMessage("调度单元添加车辆成功");
    }

    /**
     * 调度单元移除车辆
     */
    @SuppressWarnings("unchecked")
    public void removeAIVeh(String message, Response response) {
        log.debug("调度单元移除车辆,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        ArrayList<Integer> vehicleIds = BaseUtil.get(params, "vehicleIds", ArrayList.class);
        Integer unitId = BaseUtil.get(params, "unitId", Integer.class);
        Unit unit = BaseCacheUtil.getUnit(unitId);
        if (null == unit) {
            log.debug("调度单元不存在,{}", unitId);
            response.withFailMessage("调度单元不存在");
            return;
        }
        if (BaseUtil.CollectionNotNull(vehicleIds)) {
            for (Integer vehicleId : vehicleIds) {
                unit.removeVehicleTask(vehicleId);
            }
        }
        response.withSucMessage("调度单元移除车辆成功");
    }


    /**
     * 启动车辆
     */
    public void startVehTask(String message, Response response) {
        log.debug("启动车辆，{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null == vehicleTask) {
            response.withFailMessage("该车辆不存在");
            log.debug("该车辆[{}]不存在", vehicleId);
            return;
        }
        String taskState = vehicleTask.getHelper().getTaskState();
        if (InteractiveStateEnum.FREE.getValue().equals(taskState)) {
            DispatchUnitInput input = (DispatchUnitInput) InputCache.inputSetAndGet(vehicleId, DispatchUnitInput.class);
            input.runDispatchTask(vehicleId, response);
        } else {
            vehicleTask.startVehicle();
            response.withSucMessage("启动车辆成功");
        }

    }

    /**
     * 停止车辆
     */
    public void stopVehTask(String message, Response response) {
        log.debug("停止车辆，{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);
        DispatchInput input = InputCache.getDispatchInput(vehicleId);
        if(input instanceof InteractiveInput){
            CommSend.vehAutoSafeParking(vehicleId);
            response.withSucMessage("停止车辆成功");
        }else if(input instanceof DispatchUnitInput){
            VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
            boolean loadState = vehicleTask.getHelper().isDispatchLoadState();
            if(!loadState){//不是重载
                CommSend.vehAutoSafeParking(vehicleId);
                response.withSucMessage("停止车辆成功");
            }else{

                response.withSucMessage("停止下个调度任务成功");
            }
        }
    }

    /**
     * 交互式请求
     */
    @SuppressWarnings("unchecked")
    public void createPath(String message, Response response) {
        log.debug("交互式请求，{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null == vehicleTask) {
            response.withFailMessage("系统中没有改车辆信息");
            return;
        }
        Integer planType = BaseUtil.get(params, "planType", Integer.class);
        List<Map> points = BaseUtil.get(params, "points", ArrayList.class);
        InteractiveInput input = (InteractiveInput) InputCache.inputSetAndGet(vehicleId, InteractiveInput.class);
        boolean result = input.createPath(response, vehicleId, BaseUtil.map2Bean(points.get(0), Point.class), planType);
        if (!result) {
            log.debug("车辆[{}]交互式请求失败!", vehicleId);
            response.withFailMessage("交互式路径请求失败!");
        } else {
            response.withSucMessage("交互式路径生成中...");
        }
    }


    /**
     * 交互式路径运行
     */
    public void runPath(String message, Response response) {
        log.debug("交互式路径运行,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null == vehicleTask) {
            response.withFailMessage("系统中没有改车辆信息");
            return;
        }
        boolean running = InputCache.getPathManager(vehicleId).pathRunning();
        if (running) {
            response.withSucMessage("交互式路径启动成功");
        } else {
            response.withFailMessage("交互式路径启动失败");
        }
        vehicleTask.startVehicle();

    }

    /**
     * 取消交互式路径
     */
    public void stopVeh(String message, Response response) {
        log.debug("取消交互式路径,{}", message);
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null == vehicleTask) {
            response.withFailMessage("系统中没有改车辆信息");
            return;
        }
        DispatchInput input = InputCache.getDispatchInput(vehicleId);
        if (input instanceof InteractiveInput) {
            input.stopRun();
            response.withSucMessage("取消交互式路径成功");
            return;
        }
        response.withFailMessage("非交互式任务!");
    }

    /**
     * 进车信号
     */
    public void loadAreaEntry(String message, Response response) {
        log.debug("进车信号,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.setAreaState(LoadArea.LoadAreaStateEnum.READY);
        loadArea.eventPublisher(null, NotifyCommandEnum.EXCAVATOR_INOTSIGN_COMMAND);
        response.withSucMessage("进车信号接收成功");
    }

    /**
     * 出车信号
     */
    public void loadAreaWorkDone(String message, Response response) {
        log.debug("出车信号,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.setAreaState(LoadArea.LoadAreaStateEnum.DELAY);
        loadArea.eventPublisher(null, NotifyCommandEnum.EXCAVATOR_OUTSIGN_COMMAND);
        response.withSucMessage("出车信号接收成功");
    }

    /**
     * 任务点开装
     */
    public void loadAreaWorkBegin(String message, Response response) {
        log.debug("任务点开装,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.eventPublisher(null, NotifyCommandEnum.EXCAVATOR_BEGIN_LOAD_COMMAND);
        response.withSucMessage("任务点开装接收成功");
    }

    /**
     * 取消进车信号
     */
    public void loadAreaEntryCancel(String message, Response response) {
        log.debug("取消进车信号,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.setAreaState(LoadArea.LoadAreaStateEnum.DELAY);
        response.withSucMessage("取消进车信号接收成功");
    }

    /**
     * 任务点开工
     */
    public void taskSpotStart(String message, Response response) {
        log.debug("任务点开工,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.setAreaState(LoadArea.LoadAreaStateEnum.DELAY);
        response.withSucMessage("任务点开工接收成功");
    }

    /**
     * 任务点停工
     */
    public void taskSpotStop(String message, Response response) {
        log.debug("任务点停工,{}", message);
        LoadArea loadArea = getLoadArea(message);
        if (null == loadArea) {
            response.withFailMessage("装载区不存在该任务点!");
            return;
        }
        loadArea.setAreaState(LoadArea.LoadAreaStateEnum.OFFLINE);
        response.withSucMessage("任务点停工接收成功");
    }

    /**
     * 根据任务点获取装载区
     */
    private LoadArea getLoadArea(String message) {
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer taskSpotId = BaseUtil.get(params, "taskSpotId", Integer.class);
        if (null == taskSpotId) {
            return null;
        }
        Set<LoadArea> loadAreas = BaseCacheUtil.getTaskArea(AreaTypeEnum.LOAD_AREA);
        for (LoadArea loadArea : loadAreas) {
            LoadPoint loadPoint = loadArea.getLoadPoint();
            if (null != loadPoint && taskSpotId.longValue() == loadPoint.getLoadId()) {
                return loadArea;
            }
        }
        return null;
    }


    /**
     * tcltd 绕障路径运行 未完成
     */
    public void aroundCreatePath(String message, Response response) {
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer vehicleId = BaseUtil.get(params, "vehicleId", Integer.class);

    }

    /**
     * tcltd 申请更换矿种 未完成
     */
    public void changeLoadType(String message, Response response) {
        HashMap params = BaseUtil.toObj(message, HashMap.class);
        Integer unitId = BaseUtil.get(params, "unitId", Integer.class);
        Integer unLoaderAreaId = BaseUtil.get(params, "unLoaderAreaId", Integer.class);
        Integer loaderAreaId = BaseUtil.get(params, "loaderAreaId", Integer.class);
    }

}
