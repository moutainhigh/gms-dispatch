package com.baseboot.entry.global;

import com.baseboot.service.BaseCacheUtil;
import com.baseboot.common.service.MqService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Data
@Slf4j
public class RetryMessage {

    private String messageId;

    private Response response;

    private Request request;

    private int retry = 1;//运行次数

    private long expiration = 20000;//过期时间

    private Timer timer;

    private boolean isEnd = false;

    private boolean isSuccess = false;

    private Runnable execute;

    private Runnable result;

    private Date createTime;

    public RetryMessage(String messageId) {
        this.messageId = messageId;
        createTime = new Date();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isEnd = true;
                timer.cancel();
                if (null != result) {
                    result.run();
                } else {
                    fail();
                }
            }
        }, expiration);
        BaseCacheUtil.addRetryMessage(this);
    }

    public RetryMessage(int retry, String messageId) {
        this(messageId);
        this.retry = retry;
    }

    public RetryMessage withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }


    public RetryMessage withExecute(Runnable task) {
        this.execute = task;
        return this;
    }

    public RetryMessage withResponse(Response response) {
        this.response = response;
        return this;
    }

    public RetryMessage withRequest(Request request) {
        this.request = request;
        return this;
    }


    public RetryMessage setResult(Runnable result) {
        this.result = result;
        return this;
    }

    public RetryMessage withSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
        if (isSuccess) {
            retry = 0;
            start();
        }
        return this;
    }

    public void start() {
        if (null == execute) {
            log.debug("请设置处理消息的任务");
            return;
        }
        if (!isSuccess && !isEnd && retry > 0) {
            retry--;
            execute.run();
        } else if (isSuccess || retry == 0) {
            isEnd = true;
            timer.cancel();
            if (null != result) {
                result.run();
            } else {
                fail();
            }
        } else {
            log.debug("改消息已处理完成!,messageId={}", messageId);
        }
    }

    public void fail() {
        if (!isSuccess && null != response) {
            response.withFailMessage("远程服务未响应");
            MqService.response(response);
        }
    }
}
