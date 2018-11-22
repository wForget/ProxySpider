package cn.wangz.spider.proxy.server;

import cn.wangz.spider.proxy.conf.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by hadoop on 2018/11/9.
 */
public class MongoServer {
    private static MongoClient client = null;

    static {
        client = new MongoClient(MongoConfig.serverAddress);
    }

    public static MongoClient getClient() {
        return client;
    }

    public static MongoCollection<Document> getCollection(String db, String col) {
        return client.getDatabase(db).getCollection(col);
    }

    public static void close() {
        if (client != null) {
            client.close();
        }
    }
}
