package com.baseboot.entry.dispatch.monitor.vehicle;

import com.baseboot.enums.DispatchStateEnum;
import com.baseboot.enums.ModeStateEnum;
import com.baseboot.enums.TaskTypeEnum;
import com.baseboot.service.dispatch.input.InteractiveStateEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveInfo implements Serializable {

    private static final long serialVersionUID = -4525898776391287115L;

    private Integer vehicleId;

    /**
     * 车辆状态,初始手动模式
     */

    private ModeStateEnum modeState = ModeStateEnum.MANUAL_MODE;

    /**
     * 调度状态,初始空闲
     */
    private DispatchStateEnum dispState = DispatchStateEnum.NOLOAD_FREE;

    /**
     * 任务状态
     */
    private AtomicReference<String> taskState = new AtomicReference<>(InteractiveStateEnum.FREE.getValue());

    /**
     * 当前车辆所在调度单元
     */

    private Integer unitId = 0;

    /**
     * 当前任务的目标区域
     */

    private Integer taskAreaId = 0;

    /**
     * 当前任务类型
     */

    private TaskTypeEnum taskType=TaskTypeEnum.NONE;


    /**
     * 当前任务的任务点
     */

    private Integer taskSpotId = 0;

    /**
     * 是否运行
     */

    private String runFlag;

    /**
     * 是否遇到障碍物
     */

    private Boolean obsFlag;


    //路径最近点id
    private Integer nowPathId;
    //当前点到起点距离
    private Double nowDistance;
    //路径终点到起点距离
    private Double endDistance;

    /**
     * 产生时间
     */

    private Date updateTime;

    @JsonFormat(pattern = "yyyyMMddHHmmss", timezone = "GMT+8")
    public void setUpdateTime(Date time) {
        this.updateTime = time;
    }


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 监控数据
     */
    private Monitor monitor;

}
