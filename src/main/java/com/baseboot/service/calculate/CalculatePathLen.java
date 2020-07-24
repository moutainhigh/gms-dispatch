package com.baseboot.service.calculate;

import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.DispatchUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 计算默认发送路径的终点
 * {@link WorkPathInfo trailLen}
 */
@Slf4j
@CalculateClass
@Component
public class CalculatePathLen implements Calculate {

    @Override
    public int calculateEndPoint(Integer vehicleId) {
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        if (null != globalPath) {
            WorkPathInfo workPathInfo = globalPath.getWorkPathInfo();
            int nearestId = workPathInfo.getNearestId();
            int i = nearestId;
            for (; i < workPathInfo.getSectionPathEndId(); i++) {
                double dis = DispatchUtil.GetDistance(globalPath,nearestId, i);
                //发送TrailLen长度的点，如果到分段终点的长度不够TrailLen，则只发到分段终点
                if (dis > workPathInfo.getTrailLen()) {
                    break;
                }
            }
            return i;
        }
        return 10000;
    }
}
