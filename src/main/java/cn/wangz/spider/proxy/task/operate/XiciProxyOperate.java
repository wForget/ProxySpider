package cn.wangz.spider.proxy.task.operate;

import cn.wangz.spider.proxy.bean.ParseDoc;
import cn.wangz.spider.proxy.bean.ProxyUrlContent;
import cn.wangz.spider.proxy.bean.RawProxy;
import cn.wangz.spider.proxy.bean.UpdateDoc;
import cn.wangz.spider.proxy.task.SpiderOperate;
import org.bson.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * url: http://www.xicidaili.com/nt/
 *      http://www.xicidaili.com/nn/
 *
 *  {
 *      "url" : "http://www.xicidaili.com/nt/",
 *      "type" : 1,
 *      "index" : 0,
 *      "status" : 0,
 *      "num" : 0
 *  }
 *
 */
public class XiciProxyOperate implements SpiderOperate {
    private static final Logger logger = LoggerFactory.getLogger(XiciProxyOperate.class);

    @Override
    public ProxyUrlContent download(String url, Proxy proxy) {
        try {
            Connection connection = Jsoup.connect(url);
            if (proxy != null) {
                connection.proxy(proxy);
            }

            connection.timeout(5000)
                    .method(Connection.Method.GET)
                    .header("Accept", "*/*")
                    .followRedirects(true)	// 是否跟随跳转, 处理3开头的状态码
                    .ignoreHttpErrors(true)	// 是否忽略网络错误, 处理5开头的状态码
                    .ignoreContentType(true)	// 是否忽略类型, 处理图片、音频、视频等下载
                    .header("Accept-Language", "zh-CN")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");

            Connection.Response response = connection.execute();
            String realUrl = response.url().toString();

            int status = response.statusCode();
            String charset = response.charset();
            if (charset == null) charset = "utf-8";
            if (status != 404 && status != 403 && status < 500) {
                byte[] content = response.bodyAsBytes();
                if (content == null || content.length < 50) {
                    return null;
                }

                ProxyUrlContent proxyUrlContent = new ProxyUrlContent();
                proxyUrlContent.setStatusCode(status);
                proxyUrlContent.setUrl(realUrl);
                proxyUrlContent.setContent(new String(content, charset));

                return proxyUrlContent;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return null;
    }

    @Override
    public ParseDoc parse(ProxyUrlContent proxyUrlContent) {
        List<UpdateDoc> rawProxies = parseRawProxies(proxyUrlContent);
        if (rawProxies == null || rawProxies.isEmpty()) return null;
        ParseDoc parseDoc = new ParseDoc();
        parseDoc.setRawProxies(rawProxies);
        return parseDoc;
    }

//    private List<Document> parsePageUrl(ProxyUrlContent proxyUrlContent) {
//        List<Document> docList = null;
//
//        try {
//            org.jsoup.nodes.Document html = Jsoup.parse(new String(proxyUrlContent.getContent(), "utf-8")
//                    , proxyUrlContent.getUrl());
//            if (html != null) {
//                String refer = proxyUrlContent.getUrl();
//                Integer type = proxyUrlContent.getType();
//                int index = proxyUrlContent.getIndex() + 1;
//
//                int page = 6;
//
//                docList = new ArrayList<>();
//                String url = proxyUrlContent.getUrl();
//
//                for (int i = 1; i < page; i++) {
//                    Document doc = new Document();
//                    doc.append("refer", refer);
//                    doc.append("type", type);
//                    doc.append("index", index);
//                    doc.append("status", 0);
//                    doc.append("num", 0);
//                    doc.append("url", url + i);
//
//                    docList.add(doc);
//                }
//                return docList;
//            }
//        } catch (UnsupportedEncodingException e) {
//            String msg = ExceptionUtil.stackTraceMsg(e);
//            logger.error(msg);
//        }
//
//        return null;
//    }

    private List<UpdateDoc> parseRawProxies(ProxyUrlContent proxyUrlContent) {
        List<UpdateDoc> rawProxies = null;

        org.jsoup.nodes.Document html = Jsoup.parse(proxyUrlContent.getContent()
                , proxyUrlContent.getUrl());
        if (html != null) {
            Elements trelements = html.select("table#ip_list tbody tr");
            if (trelements != null && trelements.size() > 1) {
                rawProxies = new ArrayList<>();
                for (int i = 1; i < trelements.size(); i++) {
                    Elements tdelements = trelements.get(i).select("td");
                    String addr = tdelements.get(1).text();
                    int port = Integer.valueOf(tdelements.get(2).text());

                    if (addr != null && !"".equals(addr) && port > 0 && port < 65535) {
                        RawProxy rawProxy = new RawProxy();
                        rawProxy.setAddr(addr);
                        rawProxy.setPort(port);
                        rawProxy.setReferenceNum(0);

                        rawProxies.add(rawProxy);
                    }
                }
            }
        }
        return rawProxies;
    }
}
