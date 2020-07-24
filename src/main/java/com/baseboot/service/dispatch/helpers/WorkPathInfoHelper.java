package com.baseboot.service.dispatch.helpers;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.dispatch.CalculatedValue;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.Vertex;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.map.Point;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.DispatchUtil;
import com.baseboot.service.calculate.CalculateJoin;
import com.baseboot.service.dispatch.input.InputCache;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WorkPathInfoHelper implements Helper<WorkPathInfo> {

    private WorkPathInfo workPathInfo;

    private Integer vehicleId;

    @Override
    public WorkPathInfo getHelpClazz() {
        return this.workPathInfo;
    }

    @Override
    public void initHelpClazz(WorkPathInfo workPathInfo) {
        this.workPathInfo = workPathInfo;
        this.vehicleId = workPathInfo.getVehicleId();
        pathCreated();
    }

    /**
     * 路径生成由上层接口校验，这里只打印日志
     */
    public void pathCreated() {
        log.debug("车辆[{}]全局路径点数:{}", vehicleId, workPathInfo.getPathPointNum());
    }

    /**
     * 路径启动
     */
    public boolean pathRunning() {
        if (workPathInfo.isArrive()) {
            log.debug("车辆[{}]路径已到达终点!", vehicleId);
            return false;
        }
        log.debug("车辆[{}]路径启动", vehicleId);
        workPathInfo.setSendTrail(true);
        updateSectionPath();//初始化分段点
        return true;
    }

    /**
     * 是否到达路径分段点
     */
    public boolean isArriveSegmentPoint() {
        boolean result = false;
        if (workPathInfo.getNearestId() + CalculatedValue.END_DISTANCE_NUMS >= workPathInfo.getSectionPathEndId()) {
            log.debug("车辆[{}]根据剩余点个数[{}]判断到达路径分段点，nums={}", vehicleId, CalculatedValue.END_DISTANCE_NUMS, workPathInfo.getSectionPathEndId() - workPathInfo.getNearestId());
            result = true;
        }
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        double s = DispatchUtil.GetDistance(globalPath, workPathInfo.getNearestId(), workPathInfo.getSectionPathEndId());
        if (s < CalculatedValue.END_DISTANCE_THRESHOLD) {
            log.debug("车辆[{}]根据到分段终点的距离{}判断到达路径分段点,s={}", vehicleId, CalculatedValue.END_DISTANCE_THRESHOLD, s);
            result = true;
        }
        if (result) {//判断已到达
            workPathInfo.setNearestId(workPathInfo.getSectionPathEndId());
            workPathInfo.setArriveSection(true);
            workPathInfo.setSendTrail(false);
        }
        return result;
    }

    /**
     * 是否到达分段终点并离开分段终点
     */
    public boolean leaveSegmentPoint() {
        boolean segmentFlag = isArriveSegmentPoint();
        if (segmentFlag) {//到达分段终点
            if (isArriveEnd()) {//到达终点
                return false;
            }
            if (workPathInfo.isSelectionRun()) {//到达分段终点后，判断是否允许下发下段路径,等待待机指令
                int oldId = workPathInfo.getSectionPathEndId();
                updateSectionPath();
                int newId = workPathInfo.getSectionPathEndId();
                if (oldId < newId) {
                    workPathInfo.setArriveSection(false);
                    workPathInfo.setSelectionRun(false);
                    return true;
                }
                log.debug("车辆[{}]分段终点计算异常!,oldId={},newId={},nearestId={},count={}",
                        vehicleId, oldId, newId, workPathInfo.getNearestId(), workPathInfo.getPathPointNum() - 1);
            }
        }
        //未到达分段终点
        return true;
    }

    /**
     * 是否到达终点
     */
    public boolean isArriveEnd() {
        boolean result = false;
        int endId = workPathInfo.getPathPointNum() - 1;
        if (workPathInfo.getNearestId() + CalculatedValue.END_DISTANCE_NUMS >= endId) {
            log.debug("车辆[{}]根据剩余点个数[{}]判断到达终点", vehicleId, CalculatedValue.END_DISTANCE_NUMS);
            result = true;
        }
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        double s = DispatchUtil.GetDistance(globalPath, workPathInfo.getNearestId(), endId);
        if (s < CalculatedValue.END_DISTANCE_THRESHOLD) {
            log.debug("车辆[{}]根据到终点的距离{}判断到达终点", vehicleId, CalculatedValue.END_DISTANCE_THRESHOLD);
            result = true;
        }
        if (result) {//判断已到达终点
            workPathInfo.setArriveSection(true);
            workPathInfo.setArrive(true);
            workPathInfo.setSendTrail(false);
            InputCache.getPathManager(vehicleId).pathRunEnd();
        }
        return result;
    }

    /**
     * 取消路径
     */
    public boolean cancelPath() {
        return false;
    }

    /**
     * 计算最近点,并更新
     */
    public int calculateClosestPoint(Point curPoint) {
        if (null == curPoint) {
            return -1;
        }
        int nearestId = workPathInfo.getNearestId();//上次轨迹起点
        int trailEndId = workPathInfo.getTrailEndId();//轨迹终点
        int sectionPathEndId = workPathInfo.getSectionPathEndId();//路径分段终点
        trailEndId = trailEndId > sectionPathEndId ? sectionPathEndId : trailEndId;//取小值
        int id = DispatchUtil.getNearestId(curPoint, workPathInfo.getPath(), nearestId, trailEndId);
        if (nearestId > id) {
            log.debug("车辆[{}]计算最近点异常,oldId={},newId={}", vehicleId, nearestId + 1, id);
            return id;
        }
        if (id > sectionPathEndId) {
            log.error("车辆[{}]计算获得的最近点大于分段终点,nearestId={},sId={}", vehicleId, id, sectionPathEndId);
        }
        workPathInfo.setNearestId(id);
        log.debug("计算最近点->并更新:oldId={},newId={},sid={},cId={}",nearestId, id,sectionPathEndId,workPathInfo.getPathPointNum()-1);
        return id;
    }

    /**
     * 计算并更新轨迹终点
     */
    public void updateTrailEndId() {
        int trailEndId = CalculateJoin.getJoinMinId(vehicleId);
        if (trailEndId < workPathInfo.getNearestId() || trailEndId > workPathInfo.getSectionPathEndId()) {
            log.debug("车辆[{}]计算轨迹终点异常!", vehicleId);
            return;
        }
        workPathInfo.setTrailEndId(trailEndId);
    }

    /**
     * 更新最近分段终点
     */
    public void updateSectionPath() {
        workPathInfo.setSectionPathEndId(calculateSectionPath());//设置分段路径终点位置
    }

    /**
     * 计算获取最近分段终点
     */
    public int calculateSectionPath() {
        GlobalPath globalPath = BaseCacheUtil.getGlobalPath(vehicleId);
        if (null == globalPath) {
            log.error("计算最近分段终点异常,全局路径不存在!,[{}]", vehicleId);
            return 0;
        }
        List<Vertex> vertexs = globalPath.getVertexs();
        if (BaseUtil.CollectionNotNull(vertexs) && null != globalPath.getWorkPathInfo()) {
            WorkPathInfo workPathInfo = globalPath.getWorkPathInfo();
            int sectionPathEndId = workPathInfo.getSectionPathEndId();
            int nearestId = workPathInfo.getNearestId();
            if (nearestId + 1 < sectionPathEndId) {
                //还未到达分段路径终点
                return sectionPathEndId;
            }
            int i = nearestId + 1;
            if (i >= vertexs.size()) {
                //到达最终点
                return vertexs.size() - 1;
            }
            //计算下个分段路径点位置
            boolean nReverse = vertexs.get(sectionPathEndId).isReverse();
            for (; i < vertexs.size(); i++) {
                boolean sReverse = vertexs.get(i).isReverse();
                if (nReverse != sReverse) {
                    break;
                }
            }
            if (i >= vertexs.size()) {
                i = vertexs.size() - 1;
            }
            return i;
        }
        log.error("计算最近分段终点异常!,[{}]", vehicleId);
        return 0;
    }

    /**
     * 判断车子是不是能走,true为能走
     */
    public boolean judgeEnableRunning(Point curPoint) {
        int nearestId = calculateClosestPoint(curPoint);//更新轨迹起点
        boolean leave = leaveSegmentPoint();//判断是否到达分段点或终点，到达分段点之后需要收到允许下发下段轨迹的信号
        if (leave && nearestId >= 0) {
            updateTrailEndId();//跟新轨迹终点
            workPathInfo.setSendTrail(true);
            if (nearestId == workPathInfo.getTrailEndId()) {//轨迹最近点和轨迹终点相同时，最近点+1
                workPathInfo.setNearestId(nearestId + 1);
            }
        }
        return leave && nearestId >= 0;
    }


    @Override
    public String toString() {
        return "" + this.vehicleId;
    }
}
