package com.baseboot.service.dispatch.input;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.global.AbstractEventPublisher;
import com.baseboot.entry.global.EventType;
import com.baseboot.entry.global.Listener;
import com.baseboot.enums.NotifyCommandEnum;
import com.baseboot.enums.TaskCodeEnum;
import com.baseboot.interfaces.send.CommSend;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 车辆上报命令类型处理
 * {@link TaskCodeEnum}
 */
@Data
@Slf4j
public class TaskCodeCommand extends AbstractEventPublisher {

    private Integer vehicleId;

    private VehicleTaskHelper helper;

    /**
     * 处理命令之前的命令
     */
    private TaskCodeEnum curCode;

    public TaskCodeCommand(VehicleTaskHelper helper) {
        this.vehicleId = helper.getVehicleId();
        this.helper = helper;
    }

    public void receiveCommand(TaskCodeEnum newCode) {
        if (!newCode.equals(curCode)) {
            log.debug("车辆[{}]任务编号变更,oldTaskCode:{},newTaskCode:{}", vehicleId, null == curCode ? null : curCode.getDesc(), newCode.getDesc());
            switch (newCode) {
                case TASKSTANDBY:
                    taskStandby();
                    break;
                case TASKDRIVING:
                    taskDriving();
                    break;
                case TASKUNLOADMINE:
                    taskUnloadMine();
                    break;
                case TASKNORMALPARKBYTRAJECTORY:
                    taskNormalParkByTrajectory();
                    break;
            }
            this.curCode = newCode;
            sendAutoStandby();
        }
    }

    /**
     * 发送待机命令
     * */
    private void sendAutoStandby(){
        if(null!=curCode && (curCode.equals(TaskCodeEnum.TASKSTANDBY)||
                curCode.equals(TaskCodeEnum.TASKNORMALPARKBYTRAJECTORY)||
                curCode.equals(TaskCodeEnum.TASKUNLOADMINE)||
                curCode.equals(TaskCodeEnum.TASKUNLOADSOIL))){
            CommSend.vehAutoStandby(vehicleId);
        }
    }

    /**
     * 收到待机命令
     */
    public void taskStandby() {
        log.debug("车辆[{}]收到【待机命令】",vehicleId);
        WorkPathInfo workPathInfo = BaseCacheUtil.getWorkPathInfo(vehicleId);
        if (null != workPathInfo) {
            if (curCode.equals(TaskCodeEnum.TASKSTANDBY)) {//收到待机,判断路径到达分段终点
                workPathInfo.permitSelectionRun();
            }
        }

        //如果是VAK卸矿转为待机，则发送卸矿完成通知
        if(TaskCodeEnum.TASKUNLOADMINE.equals(this.curCode)){
            eventPublisher(EventType.VEHICLE_TASKCODE, NotifyCommandEnum.VEHICLE_UNLOAD_END_COMMAND);
        }

        //进入待机，清除安全停车定时器
        BaseUtil.cancelDelayTask(TimerCommand.VEHICLE_SAFE_STOP_COMMAND+vehicleId);
        //清除发送待机命令定时器
        BaseUtil.cancelDelayTask(TimerCommand.VEHICLE_AUTO_STANDBY_COMMAND+vehicleId);
    }

    /**
     * 收到轨迹跟随命令
     */
    public void taskDriving() {
        log.debug("车辆[{}]收到【轨迹跟随命令】",vehicleId);
    }

    /**
     * 收到原路径安全停车命令
     */
    public void taskNormalParkByTrajectory() {
        log.debug("车辆[{}]收到【原路径安全停车命令】",vehicleId);
        //清除定时器
        BaseUtil.cancelDelayTask(TimerCommand.VEHICLE_SAFE_STOP_COMMAND+vehicleId);
    }


    /**
     * 收到卸矿命令
     */
    public void taskUnloadMine() {
        log.debug("车辆[{}]收到【卸矿命令】",vehicleId);
        eventPublisher(EventType.VEHICLE_TASKCODE, NotifyCommandEnum.VEHICLE_UNLOAD_START_COMMAND);
    }

    @Override
    public void eventPublisher(EventType eventType, Object value) {
        for (Listener listener : getListeners()) {
            listener.stateChange(eventType, value);
        }
    }

}
