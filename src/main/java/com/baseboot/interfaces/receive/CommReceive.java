package com.baseboot.interfaces.receive;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.service.BaseCacheUtil;
import com.baseboot.entry.dispatch.monitor.vehicle.Monitor;
import com.baseboot.entry.dispatch.monitor.vehicle.VehicleTask;
import com.baseboot.common.config.BaseConfig;
import com.baseboot.entry.global.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class CommReceive {

    private final static CommReceive instance = new CommReceive();

    public static void dispense(String message, String routeKey, String messageId) {
        //log.debug("收到通讯层消息,routekey={}",routeKey);
        Response response = new Response().withRouteKey(routeKey).withMessageId(messageId).withToWho(BaseConfig.RECEIVE_COMM);
        try {
            Method method = instance.getClass().getDeclaredMethod(routeKey, String.class, Response.class);
            method.setAccessible(true);
            method.invoke(instance, message, response);
        } catch (NoSuchMethodException e) {
            log.error("CommReceive has not [{}] method", routeKey, e);
        } catch (IllegalAccessException e) {
            log.error("CommReceive access error [{}] method", routeKey, e);
        } catch (InvocationTargetException e) {
            log.error("CommReceive call error [{}] method", routeKey, e);
        }
    }

    public void vehMonitor(String message, Response response) {
        Monitor monitor = BaseUtil.toObj(message, Monitor.class);
        VehicleTask task = BaseCacheUtil.getVehicleTask(monitor.getFromVakCode());
        if (null != task) {
            task.getHelper().getMonitorManager().updateLiveInfo(monitor);
        }
    }
}
