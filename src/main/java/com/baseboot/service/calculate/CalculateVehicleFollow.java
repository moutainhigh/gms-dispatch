package com.baseboot.service.calculate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 计算车辆跟随时的终点索引(同一条路，前面有车)
 * */
@Slf4j
@CalculateClass
@Component
public class CalculateVehicleFollow implements Calculate{
    @Override
    public int calculateEndPoint(Integer vehicleId) {
        return 10000;
    }
}
