package com.baseboot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baseboot.common.service.DelayedService;
import com.baseboot.common.service.RedisService;
import com.baseboot.common.utils.Assert;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.area.*;
import com.baseboot.entry.dispatch.monitor.vehicle.Unit;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.VehicleTrail;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.global.*;
import com.baseboot.entry.map.IdPoint;
import com.baseboot.entry.map.SemiStatic;
import com.baseboot.enums.AreaTypeEnum;
import com.baseboot.service.init.InitMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BaseCacheUtil {

    /*********************************初始化数据**************************************/

    /**
     * 初始化所有车辆信息
     */
    public static synchronized void initVehicles(List<Integer> vehicleIds) {
        log.debug("初始化所有车辆信息");
        Map<Integer, VehicleTask> vehicleTaskCache = BaseCache.VEHICLE_TASK_CACHE;
        for (Integer vehicleId : vehicleIds) {
            if (vehicleTaskCache.containsKey(vehicleId)) {
                log.debug("该车辆[{}]已初始化", vehicleId);
                continue;
            }
            VehicleTask vehicleTask = new VehicleTask(vehicleId, vehicleId == 10001||vehicleId == 10002||vehicleId == 10003);
            vehicleTask.initHelper();
            vehicleTaskCache.put(vehicleId, vehicleTask);
        }
    }

    /**
     * 初始化半静态层数据
     */
    public static void initSemiStatic(String message) {
        log.debug("初始化半静态层数据");
        Map<Integer, SemiStatic> semiStaticCache = BaseCache.SEMI_STATIC_CACHE;
        JSONObject jsonObj = BaseUtil.getJsonObj(message, "message", "data");
        if (null != jsonObj) {
            JSONArray areas = jsonObj.getJSONArray("areas");
            if (null != areas) {
                for (Object area : areas) {
                    SemiStatic semiStatic = BaseUtil.toObjIEnum(area, SemiStatic.class);
                    semiStaticCache.put(semiStatic.getId(), semiStatic);
                }
            }
        }
        initTaskAreas();
        BaseUtil.cancelDelayTask(TimerCommand.MAP_LOAD_COMMAND);
        DelayedService.addTask(InitMethod::dispatchInit,100).withPrintLog(true).withDesc("调度初始化");
    }

    /**
     * 初始化所有任务区信息
     */
    public static void initTaskAreas() {
        log.debug("初始化所有任务区信息");
        Map<Integer, SemiStatic> semiStaticCache = BaseCache.SEMI_STATIC_CACHE;
        Map<Integer, TaskArea> taskAreaCache = BaseCache.TASK_AREA_CACHE;
        for (SemiStatic semiStatic : semiStaticCache.values()) {
            Integer areaId = semiStatic.getId();
            IdPoint queuePoint = semiStatic.getQueuePoint();
            SemiStatic.TaskSpot[] taskSpots = semiStatic.getTaskSpots();
            AreaTypeEnum areaType = semiStatic.getAreaType();
            if (AreaTypeEnum.LOAD_AREA.equals(areaType)) {
                LoadArea loadArea = new LoadArea();
                loadArea.setLoadAreaId(areaId);
                if (null != queuePoint) {
                    loadArea.setQueuePoint(new QueuePoint(queuePoint.getId(), queuePoint.getX(), queuePoint.getY(), queuePoint.getZ(), queuePoint.getYawAngle()));
                }
                if (BaseUtil.arrayNotNull(taskSpots)) {
                    IdPoint[] points = taskSpots[0].getPoints();
                    loadArea.setLoadPoint(new LoadPoint(points[0].getId(), points[0].getX(), points[0].getY(), points[0].getZ(), points[0].getYawAngle()));
                }
                taskAreaCache.put(areaId, loadArea);
            } else if (AreaTypeEnum.UNLOAD_MINERAL_AREA.equals(areaType)) {
                UnLoadMineralArea unLoadMineralArea = new UnLoadMineralArea();
                unLoadMineralArea.setUnloadAreaId(areaId);
                if (null != queuePoint) {
                    unLoadMineralArea.setQueuePoint(new QueuePoint(queuePoint.getId(), queuePoint.getX(), queuePoint.getY(), queuePoint.getZ(), queuePoint.getYawAngle()));
                }
                if (BaseUtil.arrayNotNull(taskSpots)) {
                    UnloadPoint[] unloadPoints = new UnloadPoint[taskSpots.length];
                    int index = 0;
                    for (SemiStatic.TaskSpot taskSpot : taskSpots) {
                        IdPoint[] points = taskSpot.getPoints();
                        unloadPoints[index] = new UnloadPoint(points[0].getId(), points[0].getX(), points[0].getY(), points[0].getZ(), points[0].getYawAngle());
                        index++;
                    }
                    unLoadMineralArea.setUnloadPoints(unloadPoints);
                }
                taskAreaCache.put(areaId, unLoadMineralArea);
            } else if (AreaTypeEnum.UNLOAD_WASTE_AREA.equals(areaType)) {
                UnLoadWasteArea unLoadWasteArea = new UnLoadWasteArea();
                if (BaseUtil.arrayNotNull(taskSpots)) {
                    UnloadPoint[] unloadPoints = new UnloadPoint[taskSpots.length];
                    for (int i = 0; i < taskSpots.length; i++) {
                        IdPoint[] points = taskSpots[i].getPoints();
                        unloadPoints[i] = new UnloadPoint(points[0].getId(), points[0].getX(), points[0].getY(), points[0].getZ(), points[0].getYawAngle());

                    }
                    unLoadWasteArea.setUnloadPoints(unloadPoints);
                }
                if (null != queuePoint) {
                    unLoadWasteArea.setQueuePoint(new QueuePoint(queuePoint.getId(), queuePoint.getX(), queuePoint.getY(), queuePoint.getZ(), queuePoint.getYawAngle()));
                }
                taskAreaCache.put(areaId, unLoadWasteArea);
            }
        }
        taskAreaCache.forEach((key,val)->{
            log.debug("初始化地图区域:{}",val.toString());
            val.updateCache();
        });
    }

    //Set<LoadArea> taskArea = getTaskArea(AreaTypeEnum.LOAD_AREA);
    @SuppressWarnings("unchecked")
    public static <T> Set<T> getTaskArea(AreaTypeEnum areaType) {
        Map<Integer, TaskArea> taskAreaCache = BaseCache.TASK_AREA_CACHE;
        return (Set<T>) taskAreaCache.values().stream().filter(ta -> {
            return areaType.equals(ta.getAreaType());
        }).map(TaskArea::getInstance).collect(Collectors.toSet());
    }

    /**
     * 获取任务区
     * */
    @SuppressWarnings("unchecked")
    public static <T> T getTaskArea(Integer taskAreaId) {
        Map<Integer, TaskArea> taskAreaCache = BaseCache.TASK_AREA_CACHE;
        return (T)taskAreaCache.get(taskAreaId);
    }

    /***********************************unit 单元**************************************/

    /**
     * 添加调度单元
     */
    public static boolean addUnit(Unit unit) {
        Assert.notNull(unit, "[unit]不能为空");
        Map<Integer, Unit> unitMap = BaseCache.UNIT_CACHE;
        Integer unitId = unit.getUnitId();
        if (unitMap.keySet().contains(unitId)) {
            log.debug("该调度单元[{}]已添加", unitId);
            return false;
        }
        log.debug("新增调度单元,{}", unit);
        BaseCache.UNIT_CACHE.put(unitId, unit);
        unit.updateCache();
        return true;
    }

    /**
     * 删除调度单元
     */
    public static boolean removeUnit(Integer unitId) {
        Map<Integer, Unit> unitMap = BaseCache.UNIT_CACHE;
        if (unitMap.keySet().contains(unitId)) {
            log.debug("删除调度单元[{}]", unitId);
            RedisService.del(BaseConstant.MONITOR_DB, RedisKeyPool.DISPATCH_UNIT + unitId);
            return true;
        }
        log.debug("该调度单元[{}]不存在", unitId);
        return false;
    }

    /**
     * 获取调度单元
     */
    public static Unit getUnit(Integer unitId) {
        Map<Integer, Unit> unitMap = BaseCache.UNIT_CACHE;
        return unitMap.get(unitId);
    }

    /******************************** 车辆 *********************************/
    /**
     * 获取调度单元
     */
    public static VehicleTask getVehicleTask(Integer vehicleId) {
        Map<Integer, VehicleTask> taskMap = BaseCache.VEHICLE_TASK_CACHE;
        return taskMap.get(vehicleId);
    }

    /**
     * 获取全局路径
     */
    public static GlobalPath getGlobalPath(Integer vehicleId) {
        Map<Integer, GlobalPath> pathCache = BaseCache.VEHICLE_PATH_CACHE;
        return pathCache.get(vehicleId);
    }

    /**
     * 新增全局路径
     */
    public static void addGlobalPath(GlobalPath globalPath) {
        if (null != globalPath && globalPath.getVehicleId() > 0) {
            Map<Integer, GlobalPath> pathCache = BaseCache.VEHICLE_PATH_CACHE;
            if (!pathCache.containsKey(globalPath.getVehicleId())) {
                pathCache.put(globalPath.getVehicleId(), globalPath);
                return;
            } else {
                log.error("全局路径存在，新增失败!");
                return;
            }
        }
        log.error("新增全局路径失败,车辆编号异常!");
    }

    /**
     * 删除全局路径
     */
    public static boolean removeGlobalPath(Integer vehicleId) {
        VehicleTask vehicleTask = getVehicleTask(vehicleId);
        if (null != vehicleTask) {
            if (!vehicleTask.isStart()) {
                Map<Integer, GlobalPath> pathCache = BaseCache.VEHICLE_PATH_CACHE;
                pathCache.remove(vehicleId);
                RedisService.del(BaseConstant.MONITOR_DB, RedisKeyPool.VAP_PATH_PREFIX + vehicleId);
                removeWorkPathInfo(vehicleId);
                removeVehicleTrail(vehicleId);
                return true;
            }
            log.error("删除全局路径失败，车辆正在运行!");
        }
        return false;
    }

    /**
     * 添加车辆轨迹
     */
    public static boolean addVehicleTrail(VehicleTrail trail) {
        if (null != trail && trail.getVehicleId() > 0) {
            Map<Integer, VehicleTrail> trailCache = BaseCache.VEHICLE_TRAIL_CACHE;
            trailCache.put(trail.getVehicleId(), trail);
            return true;
        }
        log.error("新增车辆轨迹失败,车辆编号异常!");
        return false;
    }

    /**
     * 获取车辆轨迹
     */
    public static VehicleTrail getVehicleTrail(Integer vehicleId) {
        Map<Integer, VehicleTrail> trailCache = BaseCache.VEHICLE_TRAIL_CACHE;
        return trailCache.get(vehicleId);
    }

    /**
     * 删除车辆轨迹
     */
    private static VehicleTrail removeVehicleTrail(Integer vehicleId) {
        Map<Integer, VehicleTrail> trailCache = BaseCache.VEHICLE_TRAIL_CACHE;
        return trailCache.remove(vehicleId);
    }

    /**
     * 初始化车辆路径工作信息
     */
    public static void addWorkPathInfo(WorkPathInfo workPathInfo) {
        Map<Integer, WorkPathInfo> workingPathCache = BaseCache.WORKING_PATH_CACHE;
        if (workPathInfo != null && workPathInfo.getVehicleId() > 0) {
            workingPathCache.put(workPathInfo.getVehicleId(), workPathInfo);
        }
    }

    /**
     * 获取车辆路径信息
     */
    public static WorkPathInfo getWorkPathInfo(Integer vehicleId) {
        Map<Integer, WorkPathInfo> workingPathCache = BaseCache.WORKING_PATH_CACHE;
        return workingPathCache.get(vehicleId);
    }

    private static WorkPathInfo removeWorkPathInfo(Integer vehicleId) {
        Map<Integer, WorkPathInfo> workingPathCache = BaseCache.WORKING_PATH_CACHE;
        return workingPathCache.remove(vehicleId);
    }


    /******************************** 请求消息 *********************************/

    /**
     * 获取重试请求消息
     */
    public static RetryMessage getRetryMessage(String messageId) {
        Map<String, RetryMessage> messageCache = BaseCache.RETRY_MESSAGE_CACHE;
        return messageCache.get(messageId);
    }

    /**
     * 新增重试消息
     */
    public static void addRetryMessage(RetryMessage retryMessage) {
        if (null != retryMessage && BaseUtil.StringNotNull(retryMessage.getMessageId())) {
            Map<String, RetryMessage> messageCache = BaseCache.RETRY_MESSAGE_CACHE;
            messageCache.put(retryMessage.getMessageId(), retryMessage);
            return;
        }
        log.error("新增消息失败，参数异常!");
    }

    /**
     * 获取请求消息
     */
    public static Request getRequestMessage(String messageId) {
        Map<String, Request> messageCache = BaseCache.REQUEST_MESSAGE_CACHE;
        return messageCache.get(messageId);
    }

    /**
     * 新增消息
     */
    public static void addRequestMessage(Request request) {
        if (null != request && BaseUtil.StringNotNull(request.getMessageId())) {
            Map<String, Request> messageCache = BaseCache.REQUEST_MESSAGE_CACHE;
            messageCache.put(request.getMessageId(), request);
            return;
        }
        log.error("新增消息失败，参数异常!");
    }
}
