package com.baseboot.entry.dispatch;

public class TimerCommand {

    /**
     * 等待待机命令超时
     */
    public final static long STANDBY_TIMEOUT = 4000;

    /**
     * 等待待机定时任务命令-auto_standby_#{vehicleId}
     * */
    public final static String VEHICLE_AUTO_STANDBY_COMMAND = "vehicle_auto_standby_";

    /**
     * 原路径安全停车-vehicle_safe_stop_#{vehicleId}
     * */
    public final static String VEHICLE_SAFE_STOP_COMMAND = "vehicle_safe_stop_";

    /**
     * 卸矿-vehicle_safe_stop_#{vehicleId}
     * */
    public final static String VEHICLE_AUTO_UNLOAD_COMMAND = "vehicle_auto_unload_";

    /**
     * 路径重试消息
     * */
    public final static String PATH_RETRY_COMMAND = "path_retry_";

    /**
     * 地图加载
     * */
    public final static String MAP_LOAD_COMMAND = "map_load";



}
