package cn.wangz.spider.proxy.bean;

/**
 * Created by hadoop on 2018/11/21.
 */
public class Proxy {
    private String addr;
    private int port;
    private long date;
    private int failNum;    // 失败次数
    private long referenceNum;   // 使用的次数

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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getFailNum() {
        return failNum;
    }

    public void setFailNum(int failNum) {
        this.failNum = failNum;
    }

    public long getReferenceNum() {
        return referenceNum;
    }

    public void setReferenceNum(long referenceNum) {
        this.referenceNum = referenceNum;
    }
}
