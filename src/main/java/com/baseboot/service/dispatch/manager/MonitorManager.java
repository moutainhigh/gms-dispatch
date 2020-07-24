package com.baseboot.service.dispatch.manager;

import com.baseboot.common.service.RedisService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.monitor.vehicle.LiveInfo;
import com.baseboot.entry.dispatch.monitor.vehicle.Monitor;
import com.baseboot.entry.dispatch.monitor.vehicle.Obstacle;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.global.BaseConstant;
import com.baseboot.entry.global.RedisKeyPool;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.DispatchUtil;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import com.baseboot.service.dispatch.input.ObstacleCommand;
import com.baseboot.service.dispatch.input.TaskCodeCommand;
import com.baseboot.service.dispatch.input.VakModeCommand;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 上报数据管理
 */
@Data
@Slf4j
public class MonitorManager {

    private Integer vehicleId;

    private VehicleTaskHelper helper;

    private LiveInfo liveInfo=new LiveInfo();

    private TaskCodeCommand taskCodeCommand;

    private VakModeCommand vakModeCommand;

    private ObstacleCommand obstacleCommand;

    public MonitorManager(VehicleTaskHelper helper) {
        this.helper = helper;
        this.vehicleId = helper.getVehicleId();
        this.taskCodeCommand = new TaskCodeCommand(helper);
        this.vakModeCommand = new VakModeCommand(helper);
        this.obstacleCommand = new ObstacleCommand(helper);
    }

    /**
     * 被动跟新实时信息
     */
    public void updateLiveInfo(Monitor monitor) {
        liveInfo.setMonitor(monitor);
        taskCodeCommand.receiveCommand(helper.getTaskCode());
        vakModeCommand.receiveCommand(helper.getVakMode());
        obstacleCommand.receiveCommand(liveInfo.getMonitor().getVecObstacle());
        liveInfo.setUpdateTime(new Date());
        liveInfo.setUnitId(helper.getHelpClazz().getUnitId());
        liveInfo.setVehicleId(vehicleId);
        setWebParams();
        RedisService.set(BaseConstant.MONITOR_DB, RedisKeyPool.VAP_BASE_PREFIX + vehicleId, BaseUtil.toJson(liveInfo));
        log.debug("【{}】 curPoint:{},curSpeed:{}", vehicleId, helper.getCurLocation(), helper.getCurSpeed());
    }

    /**
     * 设置当前点和起点的距离
     */
    public void setWebParams() {
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        if (null == globalPath) {
            return;
        }
        WorkPathInfo workPathInfo = globalPath.getWorkPathInfo();
        int id1 = workPathInfo.getNearestId();
        int id2 = workPathInfo.getPathPointNum() - 1;
        double dis1 = DispatchUtil.GetDistance(globalPath, 0, id1);
        double dis2 = DispatchUtil.GetDistance(globalPath, 0, id2);
        liveInfo.setNowPathId(id1);
        liveInfo.setNowDistance(dis1);
        liveInfo.setEndDistance(dis2);
    }

}
