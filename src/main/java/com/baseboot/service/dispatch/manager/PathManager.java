package com.baseboot.service.dispatch.manager;

import com.baseboot.entry.map.Point;

import com.baseboot.interfaces.send.MapSend;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.input.DispatchInput;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 路径管理
 */
@Slf4j
public class PathManager {

    private VehicleTaskHelper helper;

    private Integer vehicleId;

    private DispatchInput input;//默认交互式

    private PathStateEnum pathState = PathStateEnum.FREE;

    public PathManager(VehicleTaskHelper helper) {
        this.helper = helper;
        this.vehicleId = helper.getVehicleId();
    }

    public boolean initInput(DispatchInput input) {
        if (isFreeState()) {
            this.input = input;
            this.input.setPathManager(vehicleId,this);
            return true;
        }
        return false;
    }

    /**
     * 判断是否是空闲
     */
    public boolean isFreeState() {
        return PathStateEnum.FREE.equals(pathState)||PathStateEnum.PATH_INTERRUPT.equals(pathState);
    }

    public boolean isValState(PathStateEnum value){
        return pathState.equals(value);
    }


    private void changePathState(PathStateEnum pathState) {
        this.pathState = pathState;
    }

    /**
     * 开始路径生成
     */
    public boolean pathCreating(Point point, int planType) {
        if (!BaseCacheUtil.removeGlobalPath(helper.getVehicleId())) {
            log.debug("车辆[{}]路径请求失败，车辆正在运行状态!",vehicleId);
            return false;
        }
        clearInterrupt();
        if (!isFreeState()) {
            log.debug("车辆[{}]非空闲状态不能提交",vehicleId);
            return false;
        }
        MapSend.getGlobalPath(helper.getVehicleTask(),helper.getVehicleId(), helper.getCurLocation(), point, planType);
        changePathState(PathStateEnum.PATH_CREATING);
        log.debug("车辆[{}]生成路径请求请求中",vehicleId);
        input.startCreatePathNotify();
        return true;
    }

    /**
     * 路径生成完成
     */
    public boolean pathCreated() {
        if (!PathStateEnum.PATH_CREATING.equals(pathState)) {
            log.error("车辆[{}]不是正在生成路径状态,不能改为路径成完成状态", helper.getVehicleId());
            return false;
        }
        changePathState(PathStateEnum.PATH_CREATED);
        input.createPathSuccessNotify();
        return true;
    }

    /**
     * 路径运行
     */
    public boolean pathRunning() {
        if (!(PathStateEnum.PATH_CREATED.equals(pathState) || (PathStateEnum.PATH_INTERRUPT.equals(pathState)))) {
            log.error("车辆[{}]不是路径生成或中断状态，不能运行路径!", helper.getVehicleId());
            return false;
        }
        changePathState(PathStateEnum.PATH_RUNNING);
        helper.getVehicleTask().changeStartFlag(true);
        helper.getVehicleTask().startVehicle();
        input.startRunNotify();
        return true;
    }

    /**
     * 路径生命周期中断，停止
     */
    public boolean pathInterrupt() {
        changePathState(PathStateEnum.PATH_INTERRUPT);
        helper.getVehicleTask().changeStartFlag(false);
        helper.getVehicleTask().stopVehicle();
        input.stopRunNotify();
        return true;
    }

    /**
     * 清理路径中断状态
     * */
    public boolean clearInterrupt(){
        boolean start = helper.getVehicleTask().isStart();
        if (!start && PathStateEnum.PATH_INTERRUPT.equals(pathState)) {
            log.debug("车辆[{}]清理路径中断状态",vehicleId);
            changePathState(PathStateEnum.FREE);
            helper.getVehicleTask().stopVehicle();
            BaseCacheUtil.removeGlobalPath(helper.getVehicleId());
        }
        return !start;
    }


    /**
     * 路径运行完成
     */
    public boolean pathRunEnd() {
        if (!PathStateEnum.PATH_RUNNING.equals(pathState)) {
            log.error("车辆[{}]不是路径运行状态，不能变更为运行完成状态!", helper.getVehicleId());
            return false;
        }
        //清理路径信息
        changePathState(PathStateEnum.FREE);
        helper.getVehicleTask().changeStartFlag(false);
        helper.getVehicleTask().stopVehicle();
        BaseCacheUtil.removeGlobalPath(helper.getVehicleId());
        input.arriveNotify();
        return true;
    }

    /**
     * 路径生成异常
     */
    public boolean pathCreateError() {
        if (!PathStateEnum.PATH_CREATING.equals(pathState)) {
            log.debug("车辆[{}]不是路径生成状态，不能改为路径生成异常!", helper.getVehicleId());
            return false;
        }
        //路径生成失败，转为空闲状态
        log.error("车辆[{}]路径生成异常", helper.getVehicleId());
        changePathState(PathStateEnum.FREE);
        input.createPathErrorNotify();
        return true;
    }

    public VehicleTaskHelper getHelper() {
        return helper;
    }

    @Override
    public String toString() {
        return "" + this.helper.getVehicleId();
    }
}
