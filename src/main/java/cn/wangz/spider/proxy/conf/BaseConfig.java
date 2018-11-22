package cn.wangz.spider.proxy.conf;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by hadoop on 2018/11/8.
 */
public class BaseConfig {
    public static Config conf = null;

    static {
        conf = ConfigFactory.load("application.conf");
    }
}
