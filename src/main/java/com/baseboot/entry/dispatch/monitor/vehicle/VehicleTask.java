package com.baseboot.entry.dispatch.monitor.vehicle;

import com.baseboot.common.service.DelayedService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.monitor.Location;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.map.Point;
import com.baseboot.enums.VehicleStateEnum;
import com.baseboot.interfaces.receive.MapReceive;
import com.baseboot.interfaces.send.CommSend;
import com.baseboot.interfaces.send.MapSend;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.DispatchUtil;
import com.baseboot.service.dispatch.helpers.HelpClazz;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleTask implements Runnable, Location, HelpClazz<VehicleTaskHelper> {

    /**
     * 任务执行时间间隔,ms
     */
    private final static Integer INTERVAL = 1000;

    /**
     * 心跳间隔
     */
    private final static Integer HEART_BEAT = 2000;

    private Integer unitId;

    private Integer vehicleId;

    private VehicleTaskHelper vehicleTaskHelper;

    private VehicleStateEnum vehicleState = VehicleStateEnum.INACTIVATE;

    /**
     * 是否启动车辆
     */
    private boolean isStart = false;

    /**
     * 是否需要运行定时任务
     */
    private boolean isRun = false;

    /**
     * 运行任务
     */
    private DelayedService.Task taskRun;

    public VehicleTask(Integer vehicleId, boolean isRun) {
        this.vehicleId = vehicleId;
        this.isRun = isRun;
        if (isRun) {
            BaseUtil.timer(this::startTimer, INTERVAL);
        }
    }

    /********************************定时任务**********************************/

    /**
     * 车辆定时任务
     */
    private void startTimer() {
        log.debug("[{}]车辆定时任务启动", vehicleId);
        if (null == taskRun) {
            taskRun = DelayedService.buildTask(this);
            taskRun.withNum(-1)
                    .withPrintLog(false)
                    .withAtOnce(true)
                    .withDesc(BaseUtil.format("车辆[{}]定时任务执行,调度状态[{}]", vehicleId, vehicleTaskHelper.getLiveInfo().getDispState()));
        }
        DelayedService.addTask(taskRun, INTERVAL);
    }

    /**
     * 主动执行任务，根据车辆状态执行任务
     */
    @Override
    public void run() {
        heartbeat();
        if (isStart && getHelper().isSelfMode()) {
            boolean trajectory = getTrajectoryByIdx();
            if (!trajectory) {
                log.debug("车辆[{}]不允许下发轨迹!", vehicleId);
                setVehicleState(VehicleStateEnum.STOP);
            }
        }
    }

    /**
     * 立即执行
     */
    public void atOnce() {
        taskRun.setNextTime(BaseUtil.getCurTime());
        DelayedService.updateTask(taskRun);
    }

    /**
     * 发送心跳
     */
    private void heartbeat() {
        CommSend.heartBeat(vehicleId);
    }


    /**
     * 发送请求轨迹的命令,true为执行获取轨迹
     * {@link MapReceive#getTrajectoryByIdx}
     */
    public boolean getTrajectoryByIdx() {
        Integer activateMapId = DispatchUtil.getActivateMapId();
        if (null == activateMapId) {
            log.debug("车辆[{}]活动地图不存在!", vehicleId);
            return false;
        }
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        if (null == globalPath) {
            log.debug("车辆[{}]轨迹路径不存在!", vehicleId);
            return false;
        }
        WorkPathInfo workPathInfo = globalPath.getWorkPathInfo();
        boolean enableRunning = workPathInfo.getHelper().judgeEnableRunning(getCurLocation());
        if (enableRunning) {
            int nearestId = workPathInfo.getNearestId();
            int trailEndId = workPathInfo.getTrailEndId();
            if (nearestId >= trailEndId) {
                log.debug("车辆[{}]最近点>=轨迹终点，不请求轨迹!", vehicleId);
                return false;
            }
            setVehicleState(VehicleStateEnum.RUNNING);
            MapSend.getTrajectory(activateMapId, vehicleId, this.getHelper().getCurSpeed(), workPathInfo.getNearestId(), workPathInfo.getTrailEndId());
            return true;
        }
        log.debug("车辆[{}]轨迹不允许下发!", vehicleId);
        return false;
    }

    /**
     * 获取当前位置
     */
    @Override
    public Point getCurLocation() {
        return vehicleTaskHelper.getCurLocation();
    }

    /**
     * 获取车辆所在调度单元
     */
    private Unit getSelfUnit() {
        if (null != unitId) {
            return BaseCacheUtil.getUnit(unitId);
        }
        log.debug("该车辆没有分配调度单元,{}", vehicleId);
        return null;
    }


    /**************************************属性 获取/设置****************************************/

    /**
     * 车辆启动
     */
    public void startVehicle() {
        if (isStart) {
            WorkPathInfo workPathInfo = BaseCacheUtil.getWorkPathInfo(vehicleId);
            if (null != workPathInfo) {
                workPathInfo.getHelper().pathRunning();
            }
        }
    }

    /**
     * 车辆停止
     */
    public void stopVehicle() {
        if (null != taskRun) {
            if (!isStart) {
                WorkPathInfo workPathInfo = BaseCacheUtil.getWorkPathInfo(vehicleId);
                if (null != workPathInfo) {
                    workPathInfo.setSendTrail(false);
                }
            }
        }
    }

    /**
     * 获取车辆状态
     * */
    public VehicleStateEnum getVehicleState() {
        return vehicleState;
    }

    /**
     * 设置车辆状态
     * */
    public void setVehicleState(VehicleStateEnum vehicleState) {
        if(!this.vehicleState.equals(vehicleState)){
            log.debug("车辆[{}]状态改变,【{}】",vehicleId,vehicleState.getDesc());
            this.vehicleState = vehicleState;
        }


    }

    /**
     * 修改车辆启动标志
     */
    public void changeStartFlag(boolean isStart) {
        log.debug("修改车辆启动标志:{}={}", vehicleId, isStart);
        this.isStart = isStart;
    }

    /**
     * 修改车辆定时任务标志
     */
    public void changeRunFlag(boolean isRun) {
        log.debug("修改车辆定时任务标志:{}={}", vehicleId, isRun);
        this.isRun = isRun;
        if (isRun) {
            taskRun = null;
            startTimer();
        } else {
            if (null != taskRun) {
                taskRun.withExec(false);
            }
        }
    }

    @Override
    public String toString() {
        LiveInfo liveInfo = vehicleTaskHelper.getLiveInfo();
        return BaseUtil.format("unitId={},vehicleId={},isStart={},taskSate={},dispState={},modeState={}",
                unitId, vehicleId, isStart, liveInfo.getTaskState().get(), liveInfo.getDispState(), liveInfo.getModeState());
    }

    @Override
    public VehicleTaskHelper getHelper() {
        return this.vehicleTaskHelper;
    }

    @Override
    public void initHelper() {
        this.vehicleTaskHelper = new VehicleTaskHelper();
        this.vehicleTaskHelper.initHelpClazz(this);
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isStart() {
        return isStart;
    }


}
