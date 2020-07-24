package com.baseboot.service.init;

import com.baseboot.common.service.DelayedService;
import com.baseboot.entry.dispatch.CalculatedValue;
import com.baseboot.entry.dispatch.TimerCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class AppStartedUpRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initSystem();
    }

    private void initSystem(){
        DelayedService.addTask(InitMethod::clearCache,50).withDesc("清理缓存");
        DelayedService.Task mapLoadTask = DelayedService.buildTask(InitMethod::mapInit)
                .withTaskId(TimerCommand.MAP_LOAD_COMMAND)
                .withAtOnce(true)
                .withDesc("地图任务区初始化")
                .withNum(-1)
                .withDelay(10000)
                .withPrintLog(true);
        DelayedService.addTask(mapLoadTask);
    }


}