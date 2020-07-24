package com.baseboot.interfaces.receive;

import com.baseboot.common.service.RedisService;
import com.baseboot.common.utils.ByteUtil;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.entry.dispatch.path.GlobalPath;
import com.baseboot.entry.dispatch.path.VehicleTrail;
import com.baseboot.entry.dispatch.path.WorkPathInfo;
import com.baseboot.entry.global.BaseConstant;
import com.baseboot.entry.global.RedisKeyPool;
import com.baseboot.entry.global.Request;
import com.baseboot.entry.global.RetryMessage;
import com.baseboot.interfaces.send.CommSend;
import com.baseboot.interfaces.send.MapSend;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.service.dispatch.manager.PathManager;
import com.baseboot.service.dispatch.manager.PathStateEnum;
import com.baseboot.service.init.InitMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MapReceive {

    private final static MapReceive instance = new MapReceive();

    public static void dispense(byte[] message, String routeKey, String messageId) {
        log.debug("收到地图层消息,routeKey={}", routeKey);
        try {
            Method method = instance.getClass().getDeclaredMethod(routeKey, byte[].class, String.class);
            method.setAccessible(true);
            method.invoke(instance, message, messageId);
        } catch (NoSuchMethodException e) {
            log.error("MapReceive has not [{}] method", routeKey, e);
        } catch (IllegalAccessException e) {
            log.error("MapReceive access error [{}] method", routeKey, e);
        } catch (InvocationTargetException e) {
            log.error("MapReceive call error [{}] method", routeKey, e);
        }
    }

    /**
     * 接收半静态层数据
     * {@link InitMethod#mapInit}
     */
    public void getSemiStaticLayerInfo(byte[] message, String messageId) {
        log.debug("接收半静态层数据");
        BaseCacheUtil.initSemiStatic(new String(message));
    }

    /**
     * 接收地图全局路径
     * {@link MapSend#getGlobalPath}
     */
    public void getGlobalPath(byte[] message, String messageId) {
        RetryMessage msg = BaseCacheUtil.getRetryMessage(messageId);
        if (msg == null) {
            log.error("没有查询到消息请求消息,messageId={}", messageId);
            return;
        }
        byte[] bytes = new byte[4];
        System.arraycopy(message, 0, bytes, 0, 4);
        int firstIntEndian = ByteUtil.bytes2IntBigEndian(bytes);
        if (firstIntEndian == 1 || Integer.reverseBytes(firstIntEndian) == 1) {
            GlobalPath globalPath = GlobalPath.createGlobalPath(message);
            VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(globalPath.getVehicleId());
            PathManager pathManager = vehicleTask.getHelper().getPathManager();
            if (pathManager.isValState(PathStateEnum.PATH_CREATING)) {
                boolean result = pathManager.pathCreated();
                if (result) {
                    //返回前端消息
                    msg.withSuccess(true);
                    //插入路径
                    RedisService.set(BaseConstant.MONITOR_DB, RedisKeyPool.VAP_PATH_PREFIX + globalPath.getVehicleId(), globalPath.toDataString());
                } else {
                    BaseCacheUtil.removeGlobalPath(globalPath.getVehicleId());
                }
            }
        }else{
           log.error("【全局路径生成失败】,messageId={},message={}",messageId,new String(message));
        }
    }


    /**
     * 接收轨迹
     * {@link VehicleTask#getTrajectoryByIdx}
     */
    public void getTrajectoryByIdx(byte[] message, String messageId) {
        Request request = BaseCacheUtil.getRequestMessage(messageId);
        if (null == request) {
            log.error("非法的轨迹数据!");
            return;
        }
        VehicleTrail newTrail = VehicleTrail.createVehicleTrail(message);
        if (null != newTrail) {
            int vehicleId = newTrail.getVehicleId();
            VehicleTrail oldTrail = BaseCacheUtil.getVehicleTrail(vehicleId);
            WorkPathInfo workPathInfo = BaseCacheUtil.getWorkPathInfo(vehicleId);
            if (null == workPathInfo || !workPathInfo.permitIsSendTrail()) {//不发送轨迹
                return;
            }
            //newTrail.setNo(null != oldTrail ? oldTrail.getNo() + 1 : 0);
            //缓存
            BaseCacheUtil.addVehicleTrail(newTrail);
            //插入轨迹
            RedisService.set(BaseConstant.MONITOR_DB, RedisKeyPool.VAP_TRAIL_PREFIX + vehicleId, newTrail.toDataString(), BaseConstant.REDIS_DEPLY_TIME);
            VehicleTask vehicleTask = BaseCacheUtil.getVehicleTask(vehicleId);
            if (null == vehicleTask) {
                log.error("车辆[{}]没有初始化", vehicleId);
                return;
            }

            int num = newTrail.getVertexNum();
            CommSend.vehAutoTrailFollowing(vehicleId, newTrail.toByteArray());
            log.debug("获取轨迹:总点数{},轨迹编号:{}，轨迹终点x:{},轨迹长度s:{}", num, newTrail.getNo(), newTrail.getVertexs().get(num - 1).getX(), newTrail.getVertexs().get(num - 1).getS());
        }
    }
}