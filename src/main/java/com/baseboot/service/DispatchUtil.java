package com.baseboot.service;

import com.baseboot.common.service.RedisService;
import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.area.TaskArea;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.Path;
import com.baseboot.entry.dispatch.path.Vertex;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.global.BaseConstant;
import com.baseboot.entry.global.RedisKeyPool;
import com.baseboot.entry.map.Point;
import com.baseboot.enums.AreaTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class DispatchUtil {

    /**
     * 获取活动地图id
     */
    public static Integer getActivateMapId() {
        String str = RedisService.get(BaseConstant.KEEP_DB, RedisKeyPool.ACTIVITY_MAP);
        if (BaseUtil.StringNotNull(str)) {
            return Integer.valueOf(str);
        }
        log.error("获取活动地图id失败!");
        return null;
    }

    /**
     * 查找最近点
     */
    public static int getNearestId(Point curPoint, GlobalPath globalPath) {
        if (!BaseUtil.allObjNotNull(curPoint, globalPath)) {
            return -1;
        }
        WorkPathInfo workPathInfo = globalPath.getWorkPathInfo();
        int nearestId = workPathInfo.getNearestId();//上次轨迹起点
        int trailEndId = workPathInfo.getTrailEndId();//轨迹终点
        int sectionPathEndId = workPathInfo.getSectionPathEndId();//路径分段终点
        trailEndId = trailEndId > sectionPathEndId ? sectionPathEndId : trailEndId;//取小值
        int id = getNearestId(curPoint, globalPath, nearestId, trailEndId);
        if (nearestId > id) {
            log.debug("车辆[{}]计算最近点异常,oldId={},newId={}", globalPath.getVehicleId(), nearestId + 1, id);
            return nearestId;
        }
        workPathInfo.setNearestId(id);
        log.debug("查找最近点:{}", id);
        return id;
    }

    /**
     * 计算一个点在指定路径指定段中的最近距离
     */
    public static int getNearestId(Point targetPoint, Path path, int startId, int endId) {
        if (BaseUtil.allObjNotNull(targetPoint, path) && path.getVertexNum()>0) {
            List<Vertex> vertexs = path.getVertexs();
            endId = vertexs.size() - 1 > endId ? endId : vertexs.size() - 1;
            if (startId > endId) {
                log.error("#getNearestId:车辆[{}]索引异常,startId={},endId={}",path.getVehicleId(),startId,endId);
                return endId;
            }
            int i = startId;
            int resultId = startId;
            double resultDis = 10000;
            for (; i < endId; i++) {
                double dis = twoPointDistance(targetPoint.getX(), targetPoint.getY(), vertexs.get(i).getX(), vertexs.get(i).getY());
                if (dis < resultDis) {
                    resultDis = dis;
                    resultId = i;
                }
            }
            return resultId;
        }
        return startId;
    }

    /**
     * 计算两点间的距离
     */
    public static double twoPointDistance(Point p1, Point p2) {
        double xd = Math.pow(p1.getX() - p2.getX(), 2);
        double yd = Math.pow(p1.getY() - p2.getY(), 2);
        return Math.sqrt(xd + yd);
    }

    public static double twoPointDistance(double x1, double y1, double x2, double y2) {
        double xd = Math.pow(x1 - x2, 2);
        double yd = Math.pow(y1 - y2, 2);
        return Math.sqrt(xd + yd);
    }

    /**
     * 获取两个索引间的距离
     */
    public static double GetDistance(Path path, int startId, int endId) {
        if (null != path && startId <= endId && path.getVertexNum()>0) {
            List<Vertex> vertexs = path.getVertexs();
            endId = vertexs.size() - 1 > endId ? endId : vertexs.size() - 1;
            if (BaseUtil.CollectionNotNull(vertexs)) {
                return Math.abs(vertexs.get(endId).getS() - vertexs.get(startId).getS());
            }
        }
        if (startId > endId) {
            log.error("#GetDistance:车辆[{}]索引异常,startId={},endId={}",path.getVehicleId(),startId,endId);
            return endId;
        }
        return startId;
    }
}
