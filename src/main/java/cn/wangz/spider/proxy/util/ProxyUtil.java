package cn.wangz.spider.proxy.util;

import cn.wangz.spider.proxy.conf.MongoConfig;
import cn.wangz.spider.proxy.server.MongoServer;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import org.bson.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * Created by Administrator on 2017/8/29.
 */
public class ProxyUtil {
    private static final Logger logger = LoggerFactory.getLogger(ProxyUtil.class);


    /**
     * 获取代理
     */
    public static Proxy getProxy() {
        Document queryDoc = new Document();
        Document updateDoc = new Document("$inc", new Document("referenceNum", 1));
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.sort(new Document("referenceNum", 1));

        Document doc = MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).findOneAndUpdate(queryDoc, updateDoc, options);
        if (doc == null || doc.isEmpty()) {
            return null;
        }

        String addr = doc.getString("addr");
        int port = doc.getInteger("port", -1);
        if (addr == null || port == -1) {
            return null;
        }

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(addr, port));

        return proxy;
    }

    /**
     * addr 和 prot 转化成 java.net.Proxy 对象
     * @param addr
     * @param prot
     * @return
     */
    public static Proxy proxy(String addr, int prot) {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(addr, prot));
    }

    /**
     * 删除代理
     * @param proxy
     * @return
     */
    public static long removeProxy(Proxy proxy) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
        return removeProxy(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
    }

    /**
     * 删除代理
     * @param addr
     * @param port
     * @return
     */
    public static long removeProxy(String addr, int port) {
        Document queryDoc = new Document();
        queryDoc.put("addr", addr);
        queryDoc.put("port", port);

        return MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).deleteMany(queryDoc).getDeletedCount();
    }

    /**
     * 标记失败代理，失败超过3次后删除代理
     * @param proxy
     */
    public static void failProxy(Proxy proxy) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
        failProxy(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
    }

    /**
     * 标记失败代理，失败超过3次后删除代理
     * @param addr
     * @param port
     */
    public static void failProxy(String addr, int port) {
        Document queryDoc = new Document();
        queryDoc.put("addr", addr);
        queryDoc.put("port", port);

        Document updateDoc = new Document();
        updateDoc.put("$inc", new Document("failNum", 1));

        Document doc = MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).findOneAndUpdate(queryDoc, updateDoc);
        if (doc == null || doc.isEmpty()) return;
        int failNum = doc.getInteger("failNum", 0);
        if (failNum > 3) {
            MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).deleteMany(queryDoc);
        }
    }

    /**
     * 判断代理是否有效，通过判断能不能建立Socket连接进行判断
     * @param proxy
     * @return
     */
    public static boolean isValid(Proxy proxy) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
        return isValid(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
    }

    /**
     * 判断代理是否有效，通过判断能不能建立Socket连接进行判断
     * @param addr
     * @param port
     * @return
     */
    public static boolean isValid(String addr, int port) {
        Socket socket = null;
        try {
            socket = new Socket(addr, port);
            return true;
        } catch (IOException e) {
            logger.info("proxy[" + addr + ":" + port + "] is not valid!");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }

        return false;
    }

    /**
     * 根据需要验证的 url 和 keyword 验证代理
     * @param proxy
     * @param url
     * @param keyword
     * @return
     */
    public static boolean volidate(Proxy proxy, String url, String keyword) {
        String content = download(url, proxy);
        if (content == null) {
            return false;
        }
        return content.contains(keyword);
    }


    private static String download(String url, Proxy proxy)  {
        try {
            Connection connection = Jsoup.connect(url);
            if (proxy == null) {
                logger.error("proxy is null");
                return null;
            }
            connection.proxy(proxy);
            connection.timeout(10000)
                    .header("Accept", "*/*")
                    .followRedirects(true)	// 是否跟随跳转, 处理3开头的状态码
                    .ignoreHttpErrors(true)	// 是否忽略网络错误, 处理5开头的状态码
                    .ignoreContentType(true)	// 是否忽略类型, 处理图片、音频、视频等下载
                    .header("Accept-Language", "zh-CN")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");

            Connection.Response response = connection.execute();

            int status = response.statusCode();
            String charset = response.charset();
            if (charset == null) charset = "utf-8";

            if (status != 404 && status != 403 && status < 500) {
                byte[] content = response.bodyAsBytes();
                if (content == null || content.length < 50) {
                    return null;
                } else {
                    return new String(content, charset);
                }
            } else {
                return null;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
