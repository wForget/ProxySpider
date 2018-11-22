package cn.wangz.spider.proxy.task;

import cn.wangz.spider.proxy.task.operate.Ip66ProxyOperate;
import cn.wangz.spider.proxy.task.operate.XiciProxyOperate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hadoop on 2018/11/21.
 */
public class SpiderOperateFactory {

    private static final Map<String, SpiderOperate> OPERATE_MAP;
    static {
        OPERATE_MAP = new HashMap<>();
        OPERATE_MAP.put("xici", new XiciProxyOperate());
        OPERATE_MAP.put("text", new Ip66ProxyOperate());

    }

    public static SpiderOperate getOperate(String type) {
        return OPERATE_MAP.get(type);
    }
}
