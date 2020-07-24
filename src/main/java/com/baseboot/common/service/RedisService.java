package com.baseboot.common.service;

import com.baseboot.common.utils.BaseUtil;
import com.baseboot.entry.global.RedisKeyPool;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
public class RedisService {

    private static String HOST;
    private final static int PORT = 6379;
    private final static int TIMEOUT = 10;
    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    static {
        HOST = System.getProperty("REDIS_SERVER_HOST");
        RedisClient client = getRedisClient();
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> config = new GenericObjectPoolConfig<>();
        pool = ConnectionPoolSupport.createGenericObjectPool(client::connect, config);
    }

    public static RedisClient getRedisClient() {
        RedisURI redisURI = RedisURI.builder().withHost(HOST).withPort(PORT).withTimeout(Duration.ofMinutes(TIMEOUT)).build();
        return RedisClient.create(redisURI);
    }

    public static boolean exists(int bdIndex, String key) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.exists(key) != 0;
        } catch (Exception e) {
            log.error("redis exists error :", e);
        }
        return false;
    }

    public static boolean set(int bdIndex, String key, String value, long... delay) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            boolean ok = redisCommands.set(key, value).equals("OK");
            if (ok && delay.length > 0) {
                redisCommands.pexpire(key, delay[0]);//毫秒
            }
            return ok;
        } catch (Exception e) {
            log.error("redis set error :", e);
        }
        return false;
    }

    public static boolean hSet(int bdIndex, String key, String field, String value) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.hset(key, field, value);
        } catch (Exception e) {
            log.error("redis hSet error :", e);
        }
        return false;
    }

    public static boolean hmSet(int bdIndex, String key, Map<String, String> values) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.hmset(key, values).equals("OK");
        } catch (Exception e) {
            log.error("redis hmSet error :", e);
        }
        return false;
    }


    public static Map<String, String> getAllHash(int bdIndex, String key) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.hgetall(key);
        } catch (Exception e) {
            log.error("redis get error :", e);
        }
        return null;
    }

    public static String get(int bdIndex, String key) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.get(key);
        } catch (Exception e) {
            log.error("redis get error :", e);
        }
        return "";
    }

    public static List<String> keys(int bdIndex, String pattern) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.keys("*" + pattern + "*");
        } catch (Exception e) {
            log.error("redis keys error :", e);
        }
        return null;
    }

    public static boolean del(int bdIndex, String... key) {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(bdIndex);
            return redisCommands.del(key) != 0;
        } catch (Exception e) {
            log.error("redis del error :", e);
        }
        return false;
    }

    /**
     * 删除模糊匹配的键
     */
    public static void delPattern(int bdIndex, String pattern) {
        List<String> keys = keys(bdIndex, pattern);
        if (BaseUtil.CollectionNotNull(keys)) {
            del(bdIndex, keys.toArray(new String[]{}));
        }
    }


    public static synchronized Long generateId() {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.select(0);
            return redisCommands.incr(RedisKeyPool.REDIS_INCR);
        } catch (Exception e) {
            log.error("redis incr error :", e);
        }
        return null;
    }
}
