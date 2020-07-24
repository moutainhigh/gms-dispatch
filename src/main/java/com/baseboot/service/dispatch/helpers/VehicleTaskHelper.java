package com.baseboot.service.dispatch.helpers;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.monitor.vehicle.LiveInfo;
import com.baseboot.entry.dispatch.monitor.vehicle.Monitor;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.map.Point;
import com.baseboot.enums.DispatchStateEnum;
import com.baseboot.enums.ModeStateEnum;
import com.baseboot.enums.TaskCodeEnum;
import com.baseboot.enums.TaskTypeEnum;
import com.baseboot.service.dispatch.input.InteractiveInput;
import com.baseboot.service.dispatch.input.TaskCodeCommand;
import com.baseboot.service.dispatch.manager.MonitorManager;
import com.baseboot.service.dispatch.manager.PathManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 车辆任务辅助工具
 */
@Slf4j
public class VehicleTaskHelper implements Helper<VehicleTask> {

    private VehicleTask vehicleTask;

    private Integer vehicleId;

    private PathManager pathManager;

    private MonitorManager monitorManager;

    /**
     * 空闲状态
     */
    private final static String FREE_STATE = "6";

    public VehicleTaskHelper() {

    }

    @Override
    public VehicleTask getHelpClazz() {
        return vehicleTask;
    }

    @Override
    public void initHelpClazz(VehicleTask vehicleTask) {
        this.vehicleTask = vehicleTask;
        this.vehicleId = vehicleTask.getVehicleId();
        this.initInteractiveManager();
        this.monitorManager = new MonitorManager(this);
    }

    private void initInteractiveManager() {
        this.pathManager = new PathManager(this);
        this.pathManager.initInput(new InteractiveInput());
    }


    /*****************************监听数据更新******************************/

    public LiveInfo getLiveInfo() {
        return this.monitorManager.getLiveInfo();
    }

    public Monitor getMonitor() {
        return getLiveInfo().getMonitor();
    }

    /*****************************车辆状态get/set*****************************/

    /**
     * 设置当前任务状态
     */
    public void setCurTaskType(TaskTypeEnum taskType) {
        getLiveInfo().setTaskType(taskType);
    }


    /**
     * 获取当前上报任务编号
     */
    public TaskCodeEnum getTaskCode() {
        Monitor monitor = getLiveInfo().getMonitor();
        if (null != monitor) {
            return TaskCodeEnum.getEnum(String.valueOf(monitor.getCurrentTaskCode()));
        }
        return null;
    }

    /**
     * 获取当前车辆模式编号
     */
    public ModeStateEnum getVakMode() {
        Monitor monitor = getLiveInfo().getMonitor();
        if (null != monitor) {
            return ModeStateEnum.getEnum(String.valueOf(monitor.getVakMode()));
        }
        return null;
    }

    /**
     * 修改调度状态
     */
    public void changeDispatchState(DispatchStateEnum dispState) {
        log.debug("修改调度状态:{}={}", vehicleId, dispState.getDesc());
        getLiveInfo().setDispState(dispState);
    }

    /**
     * 获取调度状态
     */
    public DispatchStateEnum getDispatchState() {
        return getLiveInfo().getDispState();
    }

    /**
     * 修改控制模式
     */
    public void changeModeStateEnum(ModeStateEnum modeState) {
        log.debug("修改控制模式:{}={}", vehicleId, modeState.getDesc());
        getLiveInfo().setModeState(modeState);
    }

    /**
     * 获取车辆上报的控制模式
     */
    public ModeStateEnum getModeState() {
        return getLiveInfo().getModeState();
    }

    /**
     * 获取当前车辆的控制模式
     */
    public ModeStateEnum getCurModeState() {
        return monitorManager.getVakModeCommand().getCurControlMode();
    }

    /**
     * 是否是自动模式
     */
    public boolean isSelfMode() {
        return ModeStateEnum.SELF_MODE.equals(getCurModeState());
    }

    /**
     * 是否重载
     */
    public boolean isDispatchLoadState() {
        return DispatchStateEnum.isLoadState(getDispatchState());
    }

    /**
     * 空载
     */
    public  boolean isDispatchNoLoadState() {
        return DispatchStateEnum.isNoLoadState(getDispatchState());
    }

    /**
     * 修改工作状态
     */
    public void changeTaskStateEnum(String taskState, String desc, String... except) {
        log.debug("修改工作状态:{}={} 【{}】", vehicleId, taskState, desc);
        if (null != taskState) {
            if (BaseUtil.arrayNotNull(except)) {
                for (String str : except) {
                    getLiveInfo().getTaskState().compareAndSet(str, taskState);
                }
            } else {
                getLiveInfo().getTaskState().set(taskState);
            }
        }
    }

    /**
     * 修改为空闲状态
     */
    public void changeToFreeState() {
        if (!this.getHelpClazz().isStart()) {
            log.debug("车辆[{}]改为【空闲状态】", vehicleId);
            changeTaskStateEnum(FREE_STATE, "空闲");
        }
    }

    /**
     * 是否是空闲状态
     */
    public boolean isFreeState() {
        return FREE_STATE.equals(getTaskState());
    }

    /**
     * 获取工作状态
     */
    public String getTaskState() {
        return getLiveInfo().getTaskState().get();
    }

    /**
     * 获取当前速度
     */
    public double getCurSpeed() {
        Monitor monitor = getLiveInfo().getMonitor();
        if (null != monitor) {
            return monitor.getCurSpeed();
        }
        return 0;
    }

    public Point getCurLocation() {
        Monitor monitor = getLiveInfo().getMonitor();
        if (null != monitor) {
            Point point = new Point();
            point.setX(monitor.getXworld());
            point.setY(monitor.getYworld());
            point.setYawAngle(monitor.getYawAngle());
            point.setZ(0);
            return point;
        }
        return null;
    }

    /*************************** get and set *****************************/

    public VehicleTask getVehicleTask() {
        return vehicleTask;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public PathManager getPathManager() {
        return pathManager;
    }


    @Override
    public String toString() {
        return "" + this.vehicleId;
    }

    public TaskCodeCommand getTaskCodeCommand() {
        return monitorManager.getTaskCodeCommand();
    }

    public MonitorManager getMonitorManager() {
        return monitorManager;
    }
}
