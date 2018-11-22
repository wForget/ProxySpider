package cn.wangz.spider.proxy.task;

import cn.wangz.spider.proxy.bean.*;
import cn.wangz.spider.proxy.conf.MongoConfig;
import cn.wangz.spider.proxy.server.MongoServer;
import com.mongodb.BasicDBList;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by hadoop on 2018/11/21.
 */
public class SpiderTask implements Task {

    private AtomicBoolean isStop = new AtomicBoolean(false);
    private BlockingQueue<ProxyUrlContent> parseQueue = new ArrayBlockingQueue<ProxyUrlContent>(200);

    private static final UpdateOptions UPSERT_OPTIONS = new UpdateOptions().upsert(true);
    private static final BulkWriteOptions BULK_WRITE_OPTIONS = new BulkWriteOptions().ordered(false);

    class DownloadThread implements Runnable {

        @Override
        public void run() {
            String url = null;
            try {
                while (!isStop.get()) {
                    Document urlDoc = findOne();
                    if (urlDoc == null || urlDoc.isEmpty()) {
                        try {
                            Thread.sleep(300 * 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    url = urlDoc.getString("url");
                    String type = urlDoc.getString("type");


                    ProxyUrlContent proxyUrlContent = SpiderOperateFactory.getOperate(type).download(url, null);
                    if (proxyUrlContent == null) {
                        updateUrlStatus(url, 2);
                        continue;
                    }
                    proxyUrlContent.setType(type);


                    parseQueue.put(proxyUrlContent);

                    try {
                        Thread.sleep(60 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (url != null) {
                    updateUrlStatus(url, 2);
                }
            }
        }
    }

    private synchronized Document findOne(){
        BasicDBList ins = new BasicDBList();
        ins.add(0);
        ins.add(2);

        Document query = new Document();
        query.put("status", new Document("$in", ins));
        query.put("num", new Document("$lt", 3));

        Document update = new Document();
        update.put("$set",new Document("status", -1));
        update.put("$inc", new Document("num", 1));

        return MongoServer.getCollection(MongoConfig.DB, MongoConfig.URL_COL).findOneAndUpdate(query, update);
    }

    private void updateUrlStatus(String url, int status) {
        Document queryDoc = new Document();
        queryDoc.append("url", url);

        Document updateDoc = new Document();
        updateDoc.put("status", status);
        updateDoc.put("date", System.currentTimeMillis());

        MongoServer.getCollection(MongoConfig.DB, MongoConfig.URL_COL).updateMany(queryDoc, new Document("$set", updateDoc));
    }


    class ParseThread implements Runnable {

        @Override
        public void run() {
            String url = null;
            try {
                while (true) {
                    ProxyUrlContent proxyUrlContent = parseQueue.take();
                    if (proxyUrlContent.isEmpty()) {
                        break;
                    }
                    url = proxyUrlContent.getUrl();
                    String type = proxyUrlContent.getType();
                    ParseDoc parseDoc = SpiderOperateFactory.getOperate(type).parse(proxyUrlContent);

                    if (parseDoc == null) {
                        updateUrlStatus(proxyUrlContent.getUrl(), 2);
                        continue;
                    }

                    List<UpdateDoc> proxyUrls = parseDoc.getProxyUrls();
                    if (proxyUrls != null && !proxyUrls.isEmpty()) {
                        insertUpdateDocs(proxyUrls, MongoConfig.URL_COL);
                    }
                    List<UpdateDoc> rawProxies = parseDoc.getRawProxies();
                    if (rawProxies != null && !rawProxies.isEmpty()) {
                        insertUpdateDocs(rawProxies, MongoConfig.RAWPROXY_COL);
                    }
                    updateUrlStatus(url, 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (url != null) {
                    updateUrlStatus(url, 2);
                }

            }
        }

        private void insertUpdateDocs(List<UpdateDoc> updateDocs, String col) {
            List<WriteModel<Document>> writeModelList = updateDocs.stream()
                    .map(updateDoc -> new UpdateOneModel<Document>(updateDoc.getQueryDoc(), updateDoc.getUpdateDoc(), UPSERT_OPTIONS))
                    .collect(Collectors.toList());

            MongoServer.getCollection(MongoConfig.DB, col).bulkWrite(writeModelList, BULK_WRITE_OPTIONS);
        }


    }


    @Override
    public void start() throws Exception {
        Thread[] downloadThreads = new Thread[1];
        for (int i = 0; i < downloadThreads.length; i++) {
            downloadThreads[i] = new Thread(new DownloadThread(), "DownloadThread-" + i);
            downloadThreads[i].start();
        }

        Thread[] parseThreads = new Thread[1];
        for (int i = 0; i < parseThreads.length; i++) {
            parseThreads[i] = new Thread(new ParseThread(), "ParseThread-" + i);
            parseThreads[i].start();
        }

        for (int i = 0; i < downloadThreads.length; i++) {
            downloadThreads[i].join();
        }
        ProxyUrlContent emptyContent = new ProxyUrlContent();
        emptyContent.setEmpty(true);
        parseQueue.put(emptyContent);

        for (int i = 0; i < parseThreads.length; i++) {
            parseThreads[i].join();
        }
    }

    @Override
    public void close() {
        this.isStop.set(true);
    }
}
