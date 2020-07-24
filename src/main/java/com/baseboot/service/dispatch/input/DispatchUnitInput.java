package com.baseboot.service.dispatch.input;

import com.baseboot.common.service.DelayedService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.monitor.vehicle.Unit;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.global.IEnum;
import com.baseboot.entry.global.Response;
import com.baseboot.entry.global.RetryMessage;
import com.baseboot.enums.DispatchStateEnum;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.manager.PathManager;
import com.baseboot.service.dispatch.manager.UnitTaskStateManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * 调度单元输入
 */
@Slf4j
@Data
public class DispatchUnitInput extends DispatchInputAdapter<DispatchUnitStateEnum> {

    private Unit unit;

    private VehicleTask vehicleTask;

    private UnitTaskStateManager unitTaskStateManager;

    private DispatchUnitStateEnum curState;

    /**
     * 车辆到达后的处理
     */
    private Runnable arriveHandler;

    private LinkedBlockingDeque<DispatchUnitStateEnum> taskQueue = new LinkedBlockingDeque<>();

    /**
     * 调度循环任务
     * */ {
        taskQueue.add(DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT);
        taskQueue.add(DispatchUnitStateEnum.GO_LOAD_TASK_POINT);
        taskQueue.add(DispatchUnitStateEnum.PREPARE_LOAD);
        taskQueue.add(DispatchUnitStateEnum.EXEC_LOAD);
        taskQueue.add(DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT);
        taskQueue.add(DispatchUnitStateEnum.GO_UNLOAD_TASK_POINT);
        taskQueue.add(DispatchUnitStateEnum.PREPARE_UNLOAD);
        taskQueue.add(DispatchUnitStateEnum.EXEC_UNLOAD);
    }


    @Override
    public void setPathManager(Integer vehicleId, PathManager pathManager) {
        super.setPathManager(vehicleId, pathManager);
        unitTaskStateManager = new UnitTaskStateManager(getVehicleId(), this);
    }

    public boolean runDispatchTask(Integer vehicleId, Response response) {
        VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
        if (null == vehicleTask) {
            log.error("车辆[{}]没有初始化，不能运行调度任务!", vehicleId);
            response.withFailMessage("系统中没有改车辆信息!");
            return false;
        }
        this.vehicleTask = vehicleTask;
        Integer unitId = vehicleTask.getUnitId();
        if (null == unitId) {
            log.error("车辆[{}]没有分配调度单元，不能运行调度任务!", vehicleId);
            response.withFailMessage("当前车辆没有分配调度单元，不能运行调度任务!");
            return false;
        }
        this.unit = BaseCacheUtil.getUnit(unitId);
        if(unit.isFinished()){
            log.debug("车辆[{}]当前调度任务已完成!", getVehicleId());
            response.withFailMessage("当前调度任务已完成!");
            return false;
        }
        //装载区添加监听器、车辆状态监听器
        unit.getLoadArea().addListener("excavator-" + vehicleId, unitTaskStateManager);
        this.vehicleTask.getHelper().getTaskCodeCommand().addListener("vehicle-" + vehicleId, unitTaskStateManager);//设置监听
        if (!this.vehicleTask.isStart()) {
            DispatchUnitStateEnum startTask = getStartTask();
            if (null != startTask) {
                unitTaskStateManager.changeUnitState(startTask);
                vehicleTask.startVehicle();
                response.withSucMessage("启动调度任务成功");
                return true;
            }
        }
        log.error("车辆[{}]正在运行，不能执行调度任务!", getVehicleId());
        response.withFailMessage("车辆正在运行状态!");
        return false;
    }

    /**
     * 强制设置当前任务,不能直接运行
     */
    public void setCurTask(DispatchUnitStateEnum curState) {
        DispatchUnitStateEnum state = taskQueue.getFirst();
        while (null != state && !state.equals(curState)) {
            state = taskQueue.poll();
            taskQueue.addLast(state);
            state = taskQueue.getFirst();
        }
        changeTaskState(curState);
        log.debug("【车辆[{}]设置当前任务[{}]】", getVehicleId(), curState.getDesc());
    }

    /**
     * 获取当前任务
     */
    public DispatchUnitStateEnum getCurTask() {
        return taskQueue.getFirst();
    }


    @Override
    public boolean createPath(int planType) {
        String messageId = TimerCommand.PATH_RETRY_COMMAND + getVehicleId();
        new RetryMessage(messageId).setExpiration(60000);
        return super.createPath(planType);
    }

    /**
     * 调度单元车辆停止
     */
    @Override
    public void stopRun() {
        super.stopRun();
        changeTaskState(DispatchUnitStateEnum.FREE);
    }


    /**
     * 调度单元车辆开始创建路径通知
     */
    @Override
    public void startCreatePathNotify() {
        super.startCreatePathNotify();
    }

    /**
     * 调度单元车辆路径创建成功通知
     * 路径生成成功后，启动车辆
     */
    @Override
    public void createPathSuccessNotify() {
        super.createPathSuccessNotify();
        super.startRun();
    }

    /**
     * 调度单元车辆路径创建异常通知，转为空闲
     * 路径生成失败,定时请求路径
     */
    @Override
    public void createPathErrorNotify() {
        super.createPathErrorNotify();
        changeTaskState(DispatchUnitStateEnum.FREE);
        DelayedService.addTask(() -> {
            this.createPath(0);
        }, 1000);
    }

    @Override
    public void runErrorNotify() {
        super.runErrorNotify();
    }

    /**
     * 调度单元车辆开始通知
     */
    @Override
    public void startRunNotify() {
        super.startRunNotify();
    }

    /**
     * 调度单元车辆停止通知
     */
    @Override
    public void stopRunNotify() {
        super.stopRunNotify();
    }

    /**
     * 调度单元车辆到达通知，改为下个任务状态
     */
    @Override
    public void arriveNotify() {
        super.arriveNotify();
        if (null != arriveHandler) {
            log.debug("车辆[{}]运行到达处理逻辑", getVehicleId());
            Runnable handler = this.arriveHandler;
            this.arriveHandler.run();
            if (handler.equals(this.arriveHandler)) {
                this.arriveHandler = null;
            }
        }
    }

    /**
     * 判断运行调度时要执行的任务
     */
    private DispatchUnitStateEnum getStartTask() {
        if (null != vehicleTask) {
            DispatchUnitStateEnum state = getTaskCodeEnum(vehicleTask.getHelper().getTaskState());
            if (null != state && state.equals(DispatchUnitStateEnum.FREE)) {
                DispatchStateEnum dispatchState = vehicleTask.getHelper().getDispatchState();
                if (vehicleTask.getHelper().isDispatchNoLoadState()) {
                    setCurTask(DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT);
                    return DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT;
                }
                if (vehicleTask.getHelper().isDispatchLoadState()) {
                    setCurTask(DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT);
                    return DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT;
                }
            }
            return curState;
        }
        return null;
    }


    /**
     * 获取车辆当前任务状态
     */
    @Override
    public DispatchUnitStateEnum getTaskState() {
        if (null != vehicleTask) {
            String taskState = vehicleTask.getHelper().getTaskState();
            return getTaskCodeEnum(taskState);
        }
        log.error("获取当前车辆[{}]工作状态失败!", getVehicleId());
        return null;
    }

    @Override
    public void changeTaskState(IEnum<String> state,IEnum<String> ...expects) {
        DispatchUnitStateEnum unitStateEnum = (DispatchUnitStateEnum) state;
        if (!DispatchUnitStateEnum.FREE.equals(unitStateEnum)) {
            curState = unitStateEnum;
        }
        super.changeTaskState(state,expects);
    }

    @Override
    public DispatchUnitStateEnum getTaskCodeEnum(String value) {
        return DispatchUnitStateEnum.getEnum(value);
    }

    @Override
    public String getTaskCode(DispatchUnitStateEnum codeEnum) {
        return codeEnum.getValue();
    }

    @Override
    public String toString() {
        return BaseUtil.format("调度单元[{}],车辆[{}]", unit, getVehicleId());
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof DispatchUnitInput) {
            DispatchUnitInput unitInput = (DispatchUnitInput) obj;
            return this.getVehicleId().equals(unitInput.getVehicleId());
        }
        return false;
    }
}
