package com.baseboot.entry.global;

public class RedisKeyPool {

    public final static String DELAY_TASK_PREFIX = "base_delay_task_";//延时任务参数

    public final static String REDIS_INCR = "base_redis_incr";//自增键

    public final static String DISPATCH_SERVER_INIT = "dispatch_server_init";//监听调度初始化键

    public final static String VAP_BASE_PREFIX = "vap_base_";//车辆基础信息，包括障碍物信息、异常信息

    public final static String VAP_PATH_PREFIX = "vap_path_";//车辆全局路径

    public final static String ACTIVITY_MAP = "activity_map_id";//活动地图

    public final static String VAP_TRAIL_PREFIX = "vap_trail_";//车辆轨迹

    public final static String DISPATCH_TASK_AREA_PREFIX = "dispatch_task_area_";//任务区状态

    public final static String DISPATCH_UNIT = "dispatch_unit_";//调度单元状态
}
