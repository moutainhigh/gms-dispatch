package com.baseboot.entry.dispatch.area;

import com.baseboot.common.service.RedisService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.global.*;
import com.baseboot.enums.AreaTypeEnum;

import com.baseboot.enums.TaskTypeEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class LoadArea extends AbstractEventPublisher implements TaskArea {

    private Integer loadAreaId;

    private QueuePoint queuePoint;

    private LoadPoint loadPoint;

    private LoadAreaStateEnum status = LoadAreaStateEnum.OFFLINE;//装载区状态

    private AreaTypeEnum loadType = AreaTypeEnum.LOAD_AREA;

    @Override
    public Integer getTaskAreaId() {
        return loadAreaId;
    }

    @Override
    public AreaTypeEnum getAreaType() {
        return loadType;
    }

    public void setAreaState(LoadAreaStateEnum state) {
        if (null != state) {
            log.debug("装载区状态改变:areaId={},state={}", loadAreaId, state.getDesc());
            this.status = state;
            updateCache();
        }
    }

    /**
     * 判断是否可以进车
     */
    public boolean isAllowIntoVehicle() {
        return LoadAreaStateEnum.READY.equals(status);
    }

    /**
     * 挖掘机所在装载区状态改变,发布事件
     */
    @Override
    public void eventPublisher(EventType eventType, Object value) {
        for (Listener listener : getListeners()) {
            listener.stateChange(EventType.EXCAVATOR, value);
        }
    }

    /**
     * 跟新缓存中任务区状态
     */
    @Override
    public void updateCache() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", loadAreaId);
        params.put("taskType", TaskTypeEnum.LOAD.getValue());
        List<Map<String, Object>> taskSpots = new ArrayList<>();
        if (null != loadPoint) {
            Map<String, Object> taskSpot = new HashMap<>();
            taskSpot.put("id", loadPoint.getLoadId());
            taskSpot.put("state", status.getValue());
            taskSpots.add(taskSpot);
        }
        params.put("taskSpots", taskSpots);
        RedisService.set(BaseConstant.MONITOR_DB, RedisKeyPool.DISPATCH_TASK_AREA_PREFIX + loadAreaId, BaseUtil.toJson(params));
    }

    /**
     * 任务区状态：装载区
     */
    public enum LoadAreaStateEnum {

        OFFLINE("1", "离线/停工->任务点开工"),
        DELAY("2", "延迟，不能进车->可以点进车，可以停工"),
        READY("3", "任务点就绪，可以进车->只能取消进车"),
        RELEVANCE("4", "关联/装载区已有车装载，不能进车->可以取消进车"),
        PREPARE("5", "准备->开装"),
        WORKING("6", "作业/装载->完成装"),
        ERROR("7", "异常");

        private String value;

        private String desc;


        LoadAreaStateEnum(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        @JsonValue
        public String getValue() {
            return this.value;
        }

        public String getDesc() {
            return this.desc;
        }

        public static LoadAreaStateEnum getAreaState(String value) {
            for (LoadAreaStateEnum anEnum : LoadAreaStateEnum.values()) {
                if (anEnum.getValue().equals(value)) {
                    return anEnum;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return loadType.getDesc() + ":areaId=" + loadAreaId + ",queuePoint=[" + (null == queuePoint ? "" : queuePoint.toString()) + "],loadPoint=[" + (null == loadPoint ? "" : loadPoint.toString()) + "]";
    }
}
