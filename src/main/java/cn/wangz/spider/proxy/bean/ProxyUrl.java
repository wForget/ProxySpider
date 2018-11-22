package cn.wangz.spider.proxy.bean;

import org.bson.Document;

/**
 * Created by hadoop on 2018/11/21.
 */
public class ProxyUrl implements UpdateDoc {
    private String url;
    private String type;
    private int status;
    private int num;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public Document getQueryDoc() {
        Document queryDoc = new Document();
        queryDoc.put("url", url);
        return queryDoc;
    }

    @Override
    public Document getUpdateDoc() {
        Document updateDoc = new Document();
        updateDoc.put("url", url);
        updateDoc.put("type", type);
        updateDoc.put("status", status);
        updateDoc.put("num", num);
        return new Document("$set", updateDoc);
    }
}
