package com.baseboot.service.dispatch.input;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.monitor.vehicle.Obstacle;
import com.baseboot.enums.ModeStateEnum;
import com.baseboot.enums.TaskCodeEnum;
import com.baseboot.service.dispatch.helpers.VehicleTaskHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 车辆上报障碍物
 */
@Data
@Slf4j
public class ObstacleCommand {

    private Integer vehicleId;

    private VehicleTaskHelper helper;


    public ObstacleCommand(VehicleTaskHelper helper) {
        this.vehicleId = helper.getVehicleId();
        this.helper = helper;
    }

    public void receiveCommand(Obstacle[] obstacles) {
       if(BaseUtil.arrayNotNull(obstacles)){
           log.debug("车辆【{}】收到【【故障】】,num=[{}]",vehicleId,obstacles.length);
       }
    }
}
