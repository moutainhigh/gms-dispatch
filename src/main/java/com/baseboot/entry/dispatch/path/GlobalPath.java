package com.baseboot.entry.dispatch.path;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.service.BaseCacheUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class GlobalPath extends Path {

    private WorkPathInfo workPathInfo;

    /**
     * 生成全局路径
     * */
    public static GlobalPath createGlobalPath(byte[] bytes) {
        GlobalPath globalPath = new GlobalPath();
        globalPath.parseBytes2Path(bytes);
        globalPath.initWorkPathInfo();
        BaseCacheUtil.addGlobalPath(globalPath);
        return globalPath;
    }

    /**
     * 初始化工作路径信息
     */
    private void initWorkPathInfo() {
        this.workPathInfo = new WorkPathInfo();
        workPathInfo.setVehicleId(this.getVehicleId());
        workPathInfo.setNearestId(0);
        workPathInfo.setPathPointNum(this.getVertexNum());
        workPathInfo.setTrailEndId(this.getVertexNum());
        workPathInfo.setPath(this);
        workPathInfo.initHelper();
        BaseCacheUtil.addWorkPathInfo(workPathInfo);
    }


}
