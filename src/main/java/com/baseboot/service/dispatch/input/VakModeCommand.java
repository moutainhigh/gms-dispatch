package com.baseboot.service.dispatch.input;

import com.baseboot.enums.ModeStateEnum;
import com.baseboot.enums.TaskCodeEnum;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 车辆上报控制模式
 */
@Data
@Slf4j
public class VakModeCommand {

    private Integer vehicleId;

    private VehicleTaskHelper helper;

    /**
     * 当前上报模式
     */
    private ModeStateEnum curReportedMode;

    /**
     * 当前车辆控制模式
     */
    private ModeStateEnum curControlMode = ModeStateEnum.MANUAL_MODE;

    public VakModeCommand(VehicleTaskHelper helper) {
        this.vehicleId = helper.getVehicleId();
        this.helper = helper;
    }

    public void receiveCommand(ModeStateEnum newMode) {
        if (!newMode.equals(curReportedMode)) {
            //log.debug("车辆【{}】上报控制模式改变,newMode=【{}】,oldMode=【{}】", vehicleId, newMode.getDesc(), null != curReportedMode ? curReportedMode.getDesc() : "");
            switch (newMode) {
                case SILENT:
                    silentMode();
                    break;
                case MANUAL_MODE:
                    manualMode();
                    break;
                case SELF_MODE:
                    selfMode();
                    break;
                case REMOTE_MODE:
                    remoteMode();
                    break;
                case SELF_INSPECTION:
                    selfInspectionMode();
                    break;
                case ERROR:
                    error();
                    break;
            }
            curReportedMode = newMode;
        }
        modeSwitch();
    }

    /**
     * 控制模式切换
     */
    private void modeSwitch() {
        if (!helper.isFreeState()) {
            return;
        }
        TaskCodeEnum taskCode = helper.getTaskCode();
        if (null != taskCode) {
            switch (taskCode) {
                case TASKSTANDBY:
                case TASKDRIVING:
                case TASKUNLOADMINE:
                case TASKUNLOADSOIL:
                case TASKEMERGENCYPARKBYLINE:
                case TASKEMERGENCYPARKBYTRAJECTORY:
                case TASKNORMALPARKBYTRAJECTORY:
                    //切换自动模式
                    setCurControlMode(ModeStateEnum.SELF_MODE);
                    break;
                case TASKREMOTECONTROL:
                    //切换远程模式
                    setCurControlMode(ModeStateEnum.REMOTE_MODE);
                    break;
                case TASKSILENCE:
                    //切换人工模式
                    setCurControlMode(ModeStateEnum.MANUAL_MODE);
                    break;
            }
        }
    }

    public ModeStateEnum getCurControlMode() {
        return curControlMode;
    }

    public void setCurControlMode(ModeStateEnum curControlMode) {
        if (!curControlMode.equals(this.curControlMode)) {
            log.debug("车辆【{}】控制模式切换:newMode=【{}】,oldMode={}", vehicleId, curControlMode.getDesc(), this.curControlMode.getDesc());
            this.curControlMode = curControlMode;
        }

    }

    /**
     * 收到静默控制模式命令
     */
    private void silentMode() {
        log.debug("车辆[{}]收到【静默控制模式命令】", vehicleId);
    }

    /**
     * 收到手动控制模式命令
     */
    private void manualMode() {
        log.debug("车辆[{}]收到【手动控制模式命令】", vehicleId);
    }

    /**
     * 收到自动控制模式命令
     */
    private void selfMode() {
        log.debug("车辆[{}]收到【自动控制模式命令】", vehicleId);
    }

    /**
     * 收到远程控制模式命令
     */
    private void remoteMode() {
        log.debug("车辆[{}]收到【远程控制模式命令】", vehicleId);
    }

    /**
     * 收到自检控制模式命令
     */
    private void selfInspectionMode() {
        log.debug("车辆[{}]收到【自检控制模式命令】", vehicleId);
    }

    /**
     * 控制模式异常
     */
    private void error() {
        log.debug("车辆[{}]收到【控制模式异常命令】", vehicleId);
    }
}
