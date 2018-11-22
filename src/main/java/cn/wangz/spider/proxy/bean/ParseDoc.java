package cn.wangz.spider.proxy.bean;

import java.util.List;

/**
 * Created by hadoop on 2018/11/21.
 */
public class ParseDoc {
    private List<UpdateDoc> proxyUrls;
    private List<UpdateDoc> rawProxies;

    public List<UpdateDoc> getProxyUrls() {
        return proxyUrls;
    }

    public void setProxyUrls(List<UpdateDoc> proxyUrls) {
        this.proxyUrls = proxyUrls;
    }

    public List<UpdateDoc> getRawProxies() {
        return rawProxies;
    }

    public void setRawProxies(List<UpdateDoc> rawProxies) {
        this.rawProxies = rawProxies;
    }
}
