package cn.wangz.spider.proxy.conf;

import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hadoop on 2018/9/13.
 */
public class MongoConfig extends BaseConfig {

	public static String DB = conf.getString("mongo.db");

	public static List<ServerAddress> serverAddress = null;
	static {
		String cluster = conf.getString("mongo.cluster");
		List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
		String[] serverAddressStrArr = cluster.split(",");
		for (int i = 0; i < serverAddressStrArr.length; i++) {
			String serverAddressStr = serverAddressStrArr[i];
			String host = serverAddressStr.split(":")[0];
			int port = Integer.valueOf(serverAddressStr.split(":")[1]);
			serverAddresses.add(new ServerAddress(host, port));
		}
		serverAddress = serverAddresses;
	}

	public static String URL_COL = conf.getString("mongo.url.col");

	public static String RAWPROXY_COL = conf.getString("mongo.rawproxy.col");

	public static String PROXY_COL = conf.getString("mongo.proxy.col");
}
