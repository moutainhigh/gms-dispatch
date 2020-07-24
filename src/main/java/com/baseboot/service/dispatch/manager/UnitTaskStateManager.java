package com.baseboot.service.dispatch.manager;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.area.LoadArea;
import com.baseboot.entry.dispatch.area.UnLoadMineralArea;
import com.baseboot.entry.dispatch.monitor.vehicle.Unit;
import com.baseboot.entry.global.EventType;
import com.baseboot.entry.global.Listener;
import com.baseboot.entry.map.Point;
import com.baseboot.enums.DispatchStateEnum;
import com.baseboot.enums.NotifyCommandEnum;
import com.baseboot.interfaces.send.CommSend;
import com.baseboot.service.dispatch.input.DispatchUnitInput;
import com.baseboot.service.dispatch.input.DispatchUnitStateEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 调度状态管理
 */
@Data
@Slf4j
public class UnitTaskStateManager implements Listener {

    private Integer vehicleId;

    private DispatchUnitInput unitInput;

    private NotifyCommandEnum curWaitCommand = NotifyCommandEnum.NONE_COMMAND;


    public UnitTaskStateManager(Integer vehicleId, DispatchUnitInput unitInput) {
        this.vehicleId = vehicleId;
        this.unitInput = unitInput;
    }

    /**
     * 调度任务状态改变,只能设置为去装载排队点/卸载区排队点
     */
    public void changeUnitState(DispatchUnitStateEnum state) {
        if (null != state) {
            switch (state) {
                case GO_LOAD_QUEUE_POINT://去装载排队点
                    goLoadQueuePoint();
                    break;
                case LOAD_WAIT_INTO_COMMAND://等待进车信号
                    loadWaitIntoCommand();
                    break;
                case GO_LOAD_TASK_POINT://去装载点
                    goLoadTaskPoint();
                    break;
                case PREPARE_LOAD://等待装车信号
                    prepareLoad();
                    break;
                case EXEC_LOAD://执行装载
                    execLoad();
                    break;
                case GO_UNLOAD_QUEUE_POINT://去卸载排队点
                    goUnLoadQueuePoint();
                    break;
                case UNLOAD_WAIT_INTO_COMMAND://等待卸载区允许进入
                    unloadWaitIntoCommand();
                    break;
                case GO_UNLOAD_TASK_POINT://前往卸载点
                    goUnloadTaskPoint();
                    break;
                case PREPARE_UNLOAD://准备卸载
                    prepareUnload();
                    break;
                case EXEC_UNLOAD://执行卸载
                    execUnload();
                    break;
                default:
                    log.error("没有对应可执行调度任务!");
            }
        }
    }

    /********************************** 装载 ****************************************/

    /**
     * 前往装载区排队点
     */
    private void goLoadQueuePoint() {
        unitInput.changeTaskState(DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT,
                DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT,
                DispatchUnitStateEnum.EXEC_UNLOAD);
        Point endPoint = unitInput.getUnit().getLoadQueuePoint();
        if (null != endPoint) {
            log.debug("【车辆[{}]前往装载区排队点,point={}】", getVehicleId(), endPoint.toString());
            unitInput.setEndPoint(endPoint);
            unitInput.setArriveHandler(this::loadWaitIntoCommand);
            unitInput.createPath(0);
        }
    }

    /**
     * 装载区等待进车信号
     */
    private void loadWaitIntoCommand() {
        unitInput.changeTaskState(DispatchUnitStateEnum.LOAD_WAIT_INTO_COMMAND,
                DispatchUnitStateEnum.LOAD_WAIT_INTO_COMMAND,
                DispatchUnitStateEnum.GO_LOAD_QUEUE_POINT);
        log.debug("【车辆[{}]等待进车信号】", getVehicleId());
        //判断装载区状态
        LoadArea loadArea = unitInput.getUnit().getLoadArea();
        if (null != loadArea && LoadArea.LoadAreaStateEnum.READY.equals(loadArea.getStatus())) {
            //可以进车直接去装载点
            goLoadTaskPoint();
            return;
        }
        this.curWaitCommand = NotifyCommandEnum.EXCAVATOR_INOTSIGN_COMMAND;
    }

    /**
     * 收到进车信号
     */
    private void receiveIntoCommand() {
        log.debug("【车辆[{}]收到进车信号】", getVehicleId());
        //tcl DispatchStateEnum 改为装载工作
        unitInput.getVehicleTask().getHelper().changeDispatchState(DispatchStateEnum.LOAD_WORKING);
        this.curWaitCommand = NotifyCommandEnum.NONE_COMMAND;
        goLoadTaskPoint();
    }

    /**
     * 前往装载点
     */
    private void goLoadTaskPoint() {
        changeLoadAreaState(LoadArea.LoadAreaStateEnum.RELEVANCE);
        unitInput.changeTaskState(DispatchUnitStateEnum.GO_LOAD_TASK_POINT,
                DispatchUnitStateEnum.GO_LOAD_TASK_POINT,
                DispatchUnitStateEnum.LOAD_WAIT_INTO_COMMAND);
        Point endPoint = unitInput.getUnit().getLoadPoint();
        if (null != endPoint) {
            log.debug("【车辆[{}]前往装载点,point={}】", getVehicleId(), endPoint.toString());
            unitInput.setEndPoint(endPoint);
            unitInput.setArriveHandler(this::prepareLoad);
            unitInput.createPath(0);
        }
    }

    /**
     * 准备装载,等待装车信号
     */
    private void prepareLoad() {
        changeLoadAreaState(LoadArea.LoadAreaStateEnum.PREPARE);
        unitInput.changeTaskState(DispatchUnitStateEnum.PREPARE_LOAD,
                DispatchUnitStateEnum.PREPARE_LOAD,
                DispatchUnitStateEnum.GO_LOAD_TASK_POINT);
        log.debug("【车辆[{}]等待装车信号】", getVehicleId());
        this.curWaitCommand = NotifyCommandEnum.EXCAVATOR_BEGIN_LOAD_COMMAND;
    }

    /**
     * 收到装车信号
     */
    private void receivePrepareLoadCommand() {
        changeLoadAreaState(LoadArea.LoadAreaStateEnum.WORKING);
        log.debug("【车辆[{}]收到装车信号】", getVehicleId());
        this.curWaitCommand = NotifyCommandEnum.NONE_COMMAND;
        //发送装车命令到车载，等待车载命令后执行装载

        //这里暂时没有交互，直接转为执行庄子，等待出车信号
        execLoad();
    }

    /**
     * 执行装载,等待出车信号
     */
    private void execLoad() {
        unitInput.changeTaskState(DispatchUnitStateEnum.EXEC_LOAD,
                DispatchUnitStateEnum.EXEC_LOAD,
                DispatchUnitStateEnum.PREPARE_LOAD);
        log.debug("【车辆[{}]执行装载...】", getVehicleId());
        this.curWaitCommand = NotifyCommandEnum.EXCAVATOR_OUTSIGN_COMMAND;
    }

    /**
     * 收到出车信号
     */
    private void receiveOutCommand() {
        log.debug("【车辆[{}]收到出车信号】", getVehicleId());
        //tcl DispatchStateEnum 改为满载行驶
        unitInput.getVehicleTask().getHelper().changeDispatchState(DispatchStateEnum.LOAD_RUN);
        goUnLoadQueuePoint();
    }


    /********************************** 卸载 ****************************************/

    /**
     * 去卸载排队点
     */
    private void goUnLoadQueuePoint() {
        unitInput.changeTaskState(DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT,
                DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT,
                DispatchUnitStateEnum.EXEC_LOAD);
        Point endPoint = unitInput.getUnit().getUnloadQueuePoint();
        if (null != endPoint) {
            log.debug("【车辆[{}]去卸载排队点,point={}】", getVehicleId(), endPoint.toString());
            unitInput.setEndPoint(endPoint);
            unitInput.setArriveHandler(this::unloadWaitIntoCommand);
            unitInput.createPath(0);
        }
    }

    /**
     * 卸载区等待允许进车信号
     */
    private void unloadWaitIntoCommand() {
        unitInput.changeTaskState(DispatchUnitStateEnum.UNLOAD_WAIT_INTO_COMMAND,
                DispatchUnitStateEnum.UNLOAD_WAIT_INTO_COMMAND,
                DispatchUnitStateEnum.GO_UNLOAD_QUEUE_POINT);
        log.debug("【车辆[{}]卸载区等待允许进车信号】", getVehicleId());
        //判断卸载区状态
        UnLoadMineralArea unloadArea = unitInput.getUnit().getUnloadArea();
        if (null != unloadArea && UnLoadMineralArea.UnLoadMineralAreaStateEnum.ON.equals(unloadArea.getStatus())) {
            this.curWaitCommand = NotifyCommandEnum.NONE_COMMAND;
            //可以进车直接去卸载点
            goUnloadTaskPoint();
            return;
        }
        this.curWaitCommand = NotifyCommandEnum.UNLOAD_AREA_STATE_NO;
    }

    /**
     * 前往卸载点
     */
    private void goUnloadTaskPoint() {
        unitInput.changeTaskState(DispatchUnitStateEnum.GO_UNLOAD_TASK_POINT,
                DispatchUnitStateEnum.GO_UNLOAD_TASK_POINT,
                DispatchUnitStateEnum.UNLOAD_WAIT_INTO_COMMAND);
        Point endPoint = unitInput.getUnit().getUnloadPoint();
        if (null != endPoint) {
            log.debug("【车辆[{}]前往卸载点,point={}】", getVehicleId(), endPoint.toString());
            unitInput.setEndPoint(endPoint);
            unitInput.setArriveHandler(this::prepareUnload);
            unitInput.createPath(0);
        }
    }

    /**
     * 准备卸载
     */
    private void prepareUnload() {
        unitInput.changeTaskState(DispatchUnitStateEnum.PREPARE_UNLOAD,
                DispatchUnitStateEnum.PREPARE_UNLOAD
                , DispatchUnitStateEnum.GO_UNLOAD_TASK_POINT);
        log.debug("【车辆[{}]准备卸载】", getVehicleId());
        //给车载发送卸载命令
        this.curWaitCommand = NotifyCommandEnum.VEHICLE_UNLOAD_START_COMMAND;
        CommSend.vehAutoUnload(vehicleId);
    }

    /**
     * 执行卸载
     */
    private void execUnload() {
        unitInput.changeTaskState(DispatchUnitStateEnum.EXEC_UNLOAD,
                DispatchUnitStateEnum.EXEC_UNLOAD,
                DispatchUnitStateEnum.PREPARE_UNLOAD);
        //tcl DispatchStateEnum 改为卸载工作状态
        unitInput.getVehicleTask().getHelper().changeDispatchState(DispatchStateEnum.UNLOAD_WORKING);
        this.curWaitCommand = NotifyCommandEnum.VEHICLE_UNLOAD_END_COMMAND;
        log.debug("【车辆[{}]执行卸载...】", getVehicleId());
    }

    /**
     * 收到卸载开始指令
     */
    private void receiveUnloadStartCommand() {
        log.debug("【车辆[{}]收到卸载开始指令】", getVehicleId());
        //收到卸矿指令删除定时器
        BaseUtil.cancelDelayTask(TimerCommand.VEHICLE_AUTO_UNLOAD_COMMAND + vehicleId);
        execUnload();
    }

    /**
     * 收到卸载完成指令
     */
    private void receiveUnloadEndCommand() {
        log.debug("【车辆[{}]收到卸载完成指令】", getVehicleId());
        //tcl DispatchStateEnum 改为空载行驶
        unitInput.getVehicleTask().getHelper().changeDispatchState(DispatchStateEnum.NOLOAD_RUN);
        runNextCycle();
    }

    /**
     * 开始下个循环
     */
    private void runNextCycle() {
        //通知调度单元完成一次循环任务
        boolean finishOneTask = unitInput.getUnit().vehicleFinishOneTask(vehicleId);
        if (!finishOneTask) {
            log.debug("【车辆[{}]开始下个循环】", getVehicleId());
            goLoadQueuePoint();
        }
    }

    /**
     * 修改装载区状态
     */
    private void changeLoadAreaState(LoadArea.LoadAreaStateEnum state) {
        Unit unit = unitInput.getUnit();
        if (null != unit && null != state) {
            LoadArea loadArea = unit.getLoadArea();
            if (null != loadArea) {
                loadArea.setAreaState(state);
            }
        }
    }


    /**
     * 车辆、挖掘机、卸点状态改变
     */
    @Override
    public void stateChange(EventType type, Object value) {
        NotifyCommandEnum command = (NotifyCommandEnum) value;
        log.debug("监听数据值,eventType={},value={}", type, command);
        if (null == this.curWaitCommand || !this.curWaitCommand.equals(command)) {
            log.debug("车辆[{}]接收到异常通知[{}],waitNotify=[{}]", vehicleId, command, this.curWaitCommand);
            return;
        }
        switch (command) {
            case EXCAVATOR_INOTSIGN_COMMAND:
                receiveIntoCommand();
                break;
            case EXCAVATOR_BEGIN_LOAD_COMMAND:
                receivePrepareLoadCommand();
                break;
            case EXCAVATOR_OUTSIGN_COMMAND:
                receiveOutCommand();
                break;
            case VEHICLE_UNLOAD_START_COMMAND://开始卸载
                receiveUnloadStartCommand();
                break;
            case VEHICLE_UNLOAD_END_COMMAND://卸载完成
                receiveUnloadEndCommand();
                break;
        }
    }
}
