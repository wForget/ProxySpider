package cn.wangz.spider.proxy.task;

import cn.wangz.spider.proxy.conf.MongoConfig;
import cn.wangz.spider.proxy.server.MongoServer;
import cn.wangz.spider.proxy.util.ProxyUtil;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hadoop on 2018/11/21.
 */
public class VolidateTask implements Task {

    private static Logger logger = LoggerFactory.getLogger(VolidateTask.class);

    private String validateUrl = "https://www.baidu.com/";
    private String validateKeyword = "baidu";
    private AtomicBoolean isStop = new AtomicBoolean(false);

    public VolidateTask(String validateUrl, String validateKeyword) {
        this.validateUrl = validateUrl;
        this.validateKeyword = validateKeyword;
    }


    private BlockingQueue<Document> rawProxyDocQueue = new ArrayBlockingQueue<Document>(100);

    class Query implements Runnable {

        @Override
        public void run() {
            try {
                while (!isStop.get()) {
                    Document doc = findOne();
                    if (doc == null || doc.isEmpty()) {
                        Thread.sleep(10 * 1000);
                        continue;
                    }
                    rawProxyDocQueue.put(doc);
                }

            } catch (Exception e) {
                e.printStackTrace();
                close();
            } finally {
                putEmptyDoc();
            }
        }

        private Document findOne() {
            Document queryDoc = new Document();
            queryDoc.put("referenceNum", 0);

            Document updateDoc = new Document();
            updateDoc.put("$inc", new Document("referenceNum", 1));
            return MongoServer.getCollection(MongoConfig.DB, MongoConfig.RAWPROXY_COL)
                    .findOneAndUpdate(queryDoc, updateDoc);
        }


        private void putEmptyDoc() {
            try {
                rawProxyDocQueue.put(new Document());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Validate implements Runnable {

        @Override
        public void run() {
            try {
                int validCount = 0;
                int invalidCount = 0;
                while (true) {
                    Document rawProxyDoc = rawProxyDocQueue.take();
                    if (rawProxyDoc.isEmpty()) {
                        rawProxyDocQueue.put(rawProxyDoc);
                        break;
                    }
                    String addr = rawProxyDoc.getString("addr");
                    int port = rawProxyDoc.getInteger("port");
                    Proxy proxy = ProxyUtil.proxy(addr, port);

                    boolean flag = ProxyUtil.volidate(proxy, validateUrl, validateKeyword);
                    if (flag) {
                        insertProxyCol(rawProxyDoc);
                        updateRawProxyCol(proxy);

                        validCount++;
                        logger.info(proxy + " is valid!!!");
                    } else {
                        invalidCount++;
                        logger.info(proxy + " is Invalid!!!");
                    }
                }

                logger.info("Validate end, validCount:" + validCount + ", invalidCount:" + invalidCount);
            } catch (Exception e) {
                logger.error("VolidateTask Validate Exception:\n" + e.getMessage());
                close();
            }
        }

        private void insertProxyCol(Document doc) {
            Document sortDoc = new Document("referenceNum", -1);
            Document referenceNumDoc = MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).find().sort(sortDoc).first();
            long referenceNum = (referenceNumDoc == null || referenceNumDoc.isEmpty())? 0L: referenceNumDoc.getLong("referenceNum");

            String addr = doc.getString("addr");
            int port = doc.getInteger("port");

            Document queryDoc = new Document();
            queryDoc.put("addr", addr);
            queryDoc.put("port", port);

            Document updateDoc = new Document();
            updateDoc.put("addr", addr);
            updateDoc.put("port", port);
            updateDoc.put("failNum", 0);
            updateDoc.put("referenceNum", referenceNum);
            updateDoc.put("date", System.currentTimeMillis());

            Document upsertDoc = new Document();
            upsertDoc.put("$set", updateDoc);

            UpdateOptions upsertOption = new UpdateOptions();
            upsertOption.upsert(true);

            MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).updateOne(queryDoc, upsertDoc, upsertOption);
        }

        private void updateRawProxyCol(Proxy proxy) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
            Document queryDoc = new Document();
            queryDoc.put("addr", inetSocketAddress.getHostString());
            queryDoc.put("port", inetSocketAddress.getPort());

            Document updateDoc = new Document();
            updateDoc.put("date", System.currentTimeMillis());

            MongoServer.getCollection(MongoConfig.DB, MongoConfig.RAWPROXY_COL).updateMany(queryDoc, new Document("$set", updateDoc));
        }
    }


    class MonitorProxyNumTask extends TimerTask {

        private long lastUpdateRawProxyTime = 0L;

        @Override
        public void run() {
            try {
                long validProxyNum = countValidProxy();
                if (validProxyNum < 20) {
                    logger.info("Proxy Num is less than 20!");

                    updateSpiderUrl();

                    if (System.currentTimeMillis() - lastUpdateRawProxyTime > 7200000) {
                        updateRawProxyReferenceNum();
                    }
                }

                // 清除过时的 rawproxy
                clearOutOfDateRawProxy();

            } catch (Exception e) {
                logger.error(e.getMessage());
                close();
            }
        }

        private long countValidProxy() {
            return MongoServer.getCollection(MongoConfig.DB, MongoConfig.PROXY_COL).count();
        }

        private void updateRawProxyReferenceNum() {
            Document queryDoc = new Document();
            queryDoc.put("referenceNum", new Document("$gt", 0));

            Document updateDoc = new Document();
            updateDoc.put("$set", new Document("referenceNum", 0));

            MongoServer.getCollection(MongoConfig.DB, MongoConfig.RAWPROXY_COL).updateMany(queryDoc, updateDoc);
            lastUpdateRawProxyTime = System.currentTimeMillis();
        }


        private void updateSpiderUrl() {
            Document queryDoc = new Document();
            queryDoc.put("index", new Document("$ne", 0));
            queryDoc.put("date", new Document("$lt", new DateTime().plusDays(-1).getMillis()));

            Document updateSetDoc = new Document();
            updateSetDoc.put("status", 0);
            updateSetDoc.put("num", 0);

            Document updateDoc = new Document();
            updateDoc.put("$set", updateSetDoc);

            MongoServer.getCollection(MongoConfig.DB, MongoConfig.URL_COL).updateMany(queryDoc, updateDoc);
        }

        private void clearOutOfDateRawProxy() {
            Document queryDoc = new Document();
            queryDoc.put("date", new Document("$lt", new DateTime().plusDays(-1).getMillis()));
            MongoServer.getCollection(MongoConfig.DB, MongoConfig.RAWPROXY_COL).deleteMany(queryDoc);
        }
    }

    private Timer monitorProxyNumTimer = new Timer("MonitorProxyNumTimer");

    @Override
    public void start() throws Exception {
        try {
            monitorProxyNumTimer.schedule(new MonitorProxyNumTask()
                    , new Date(), 60 * 1000);

            Thread queryThread = new Thread(new Query(), "QueryThread");
            queryThread.start();

            Thread[] validateThreadArr = new Thread[10];
            for (int i = 0; i < validateThreadArr.length; i++) {
                validateThreadArr[i] = new Thread(new Validate(), "ValidateThread-" + i);
                validateThreadArr[i].start();
            }

            for (int i = 0; i < validateThreadArr.length; i++) {
                validateThreadArr[i].join();
            }
            queryThread.join();

        } finally {
            close();
        }
    }


    @Override
    public void close() {
        this.isStop.set(true);

        if (monitorProxyNumTimer != null) {
            monitorProxyNumTimer.cancel();
        }
    }
}
