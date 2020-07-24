package com.baseboot.service.dispatch.input;

import com.baseboot.common.service.MqService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.TimerCommand;
import com.baseboot.entry.global.Response;
import com.baseboot.entry.global.RetryMessage;
import com.baseboot.entry.map.Point;
import com.baseboot.service.dispatch.manager.PathManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 交互式输入
 */
@Slf4j
public class InteractiveInput extends DispatchInputAdapter<InteractiveStateEnum> {

    private Response response;

    /**
     * 交互式创建路径,入口
     * */
    public boolean createPath(Response response,Integer vehicleId , Point point, int planType) {
        if (!BaseUtil.allObjNotNull(response, vehicleId, point)) {
            return false;
        }
        this.response = response;
        setEndPoint(point);
        String messageId = TimerCommand.PATH_RETRY_COMMAND + vehicleId;
        new RetryMessage(messageId).withResponse(response);
        log.debug("车辆[{}]交互式路径请求...", vehicleId);
        PathManager pathManager = InputCache.getPathManager(vehicleId);
        if (null != pathManager) {
            if (pathManager.clearInterrupt()) {
                return super.createPath(planType);
            }
        }
        return false;
    }


    @Override
    public void startRun() {
        super.startRun();
    }

    /**
     * 交互式停止
     * */
    @Override
    public void stopRun() {
        super.stopRun();
        changeTaskState(InteractiveStateEnum.FREE);
    }


    /**
     * 交互式开始创建路径通知
     * */
    @Override
    public void startCreatePathNotify() {
        super.startCreatePathNotify();
        changeTaskState(InteractiveStateEnum.PATH_CREATING,InteractiveStateEnum.FREE);
    }

    /**
     * 交互式路径创建成功通知
     * */
    @Override
    public void createPathSuccessNotify() {
        super.createPathSuccessNotify();
        changeTaskState(InteractiveStateEnum.PATH_CREATED_SUCESS,InteractiveStateEnum.PATH_CREATING);
        if (null != response) {
            response.withSucMessage("全局路径生成成功");
            MqService.response(response);
        }
    }

    /**
     * 交互式路径创建异常通知，转为空闲
     * */
    @Override
    public void createPathErrorNotify() {
        super.createPathErrorNotify();
        changeTaskState(InteractiveStateEnum.FREE);
    }

    @Override
    public void runErrorNotify() {
        super.runErrorNotify();
    }

    /**
     * 交互式开始通知
     * */
    @Override
    public void startRunNotify() {
        super.startRunNotify();
        this.response = null;
        changeTaskState(InteractiveStateEnum.PATH_RUNING,InteractiveStateEnum.PATH_CREATED_SUCESS);
    }

    /**
     * 交互式停止通知
     * */
    @Override
    public void stopRunNotify() {
        super.stopRunNotify();
        changeTaskState(InteractiveStateEnum.PATH_INTERRUPT);
    }

    /**
     * 交互式到达通知，改为空闲
     * */
    @Override
    public void arriveNotify() {
        super.arriveNotify();
        changeTaskState(InteractiveStateEnum.FREE);
    }

    @Override
    public InteractiveStateEnum getTaskCodeEnum(String value) {
        return InteractiveStateEnum.getEnum(value);
    }

    @Override
    public String getTaskCode(InteractiveStateEnum codeEnum) {
        return codeEnum.getValue();
    }
}
