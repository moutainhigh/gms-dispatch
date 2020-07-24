package com.baseboot.service.calculate;

import com.baseboot.entry.dispatch.monitor.Location;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.global.BaseCache;
import com.baseboot.service.BaseCacheUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 移动授权终点
 */
public interface Calculate {

    /**
     * 获取终点路径点编号索引
     */
    int calculateEndPoint(Integer vehicleId);

    /**
     * 获取系统中所有车
     */
    default Collection<VehicleTask> getSystemAllVehicles() {
        return BaseCache.VEHICLE_TASK_CACHE.values();
    }

    /**
     * 获取系统中所有具有定位信息的对象
     */
    default Collection<Location> getSysAllLocation() {
        return null;
    }

    /**
     * 获取所有正在运行的矿车的路劲
     */
    default Collection<GlobalPath> getVehicleGlobalPaths() {
        Collection<VehicleTask> vehicles = getSystemAllVehicles();
        List<GlobalPath> globalPaths = new ArrayList<>();
        Iterator<VehicleTask> iterator = vehicles.iterator();
        while (iterator.hasNext()) {
            VehicleTask next = iterator.next();
            if (next.isStart()) {
                GlobalPath path = BaseCacheUtil.getGlobalPath(next.getVehicleId());
                if(null!=path){
                    globalPaths.add(path);
                }
            }
        }
        return globalPaths;
    }
}
