package com.baseboot.entry.global;

import com.baseboot.entry.dispatch.area.TaskArea;
import com.baseboot.entry.dispatch.monitor.vehicle.Unit;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.VehicleTrail;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.map.SemiStatic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局缓存对象
 * */
public class BaseCache {

    /**
     * 消息重试实体map<messageId,MessageEntry>
     * */
    public final static Map<String, RetryMessage> RETRY_MESSAGE_CACHE =new ConcurrentHashMap<>();

    /**
     * 消息请求实体map<messageId,Request>
     * */
    public final static Map<String, Request> REQUEST_MESSAGE_CACHE =new ConcurrentHashMap<>();

    /**
     * 调度单元map<unitId,Unit>
     * */
    public final static Map<Integer, Unit> UNIT_CACHE=new ConcurrentHashMap<Integer, Unit>();

    /**
     * 系统中经过初始化的车辆任务信息<vehicleId,VehicleTask>
     * */
    public final static Map<Integer, VehicleTask> VEHICLE_TASK_CACHE=new ConcurrentHashMap<>();

    /**
     * 系统中所有任务区和任务点<taskAreaId,TaskArea>
     * */
    public final static Map<Integer,TaskArea> TASK_AREA_CACHE=new ConcurrentHashMap<>();

    /**
     * 初始化活动地图静态层信息<taskAreaId,SemiStatic>
     * */
    public final static Map<Integer, SemiStatic> SEMI_STATIC_CACHE=new ConcurrentHashMap<>();

    /**
     * 车辆当前路径<vehicleId,GlobalPath>
     * */
    public final static Map<Integer, GlobalPath> VEHICLE_PATH_CACHE=new ConcurrentHashMap<>();

    /**
     * 车辆当前轨迹<vehicleId,VehicleTrail>
     * */
    public final static Map<Integer, VehicleTrail> VEHICLE_TRAIL_CACHE=new ConcurrentHashMap<>();

    /**
     * 当前工作路径信息<vehicleId,WorkPathInfo>
     * */
    public final static Map<Integer, WorkPathInfo> WORKING_PATH_CACHE=new ConcurrentHashMap<>();

}
