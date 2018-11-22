package cn.wangz.spider.proxy.bean;

import org.bson.Document;

/**
 * Created by hadoop on 2018/11/21.
 */
public class RawProxy implements UpdateDoc {
    private String addr;
    private int port;
    private int referenceNum = 0;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReferenceNum() {
        return referenceNum;
    }

    public void setReferenceNum(int referenceNum) {
        this.referenceNum = referenceNum;
    }

    @Override
    public Document getQueryDoc() {
        Document queryDoc = new Document();
        queryDoc.put("addr", addr);
        queryDoc.put("port", port);
        return queryDoc;
    }

    @Override
    public Document getUpdateDoc() {
        Document updateDoc = new Document();
        updateDoc.put("addr", addr);
        updateDoc.put("port", port);
        updateDoc.put("referenceNum", referenceNum);
        updateDoc.put("date", System.currentTimeMillis());
        return new Document("$set", updateDoc);
    }
}
