package cn.wangz.spider.proxy.task.operate;

import cn.wangz.spider.proxy.bean.ParseDoc;
import cn.wangz.spider.proxy.bean.ProxyUrlContent;
import cn.wangz.spider.proxy.bean.RawProxy;
import cn.wangz.spider.proxy.bean.UpdateDoc;
import cn.wangz.spider.proxy.task.SpiderOperate;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * 免费HTTP代理 url : http://www.66ip.cn/mo.php?sxb=&tqsl=1000&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=http%3A%2F%2Fwww.66ip.cn%2F%3Fsxb%3D%26tqsl%3D1000%26ports%255B%255D2%3D%26ktip%3D%26sxa%3D%26radio%3Dradio%26submit%3D%25CC%25E1%2B%2B%25C8%25A1
 *
 * 免费高匿HTTPS代理 url : http://www.66ip.cn/nmtq.php?getnum=300&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=1&api=66ip
 *
 * url : http://www.89ip.cn/apijk/?&tqsl=1000&sxa=&sxb=&tta=&ports=&ktip=&cf=1
 *
 */
public class Ip66ProxyOperate implements SpiderOperate {

    private static final Logger logger = LoggerFactory.getLogger(Ip66ProxyOperate.class);

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

    private List<UpdateDoc> parseRawProxies(ProxyUrlContent proxyUrlContent) {
        List<UpdateDoc> rawProxies = new ArrayList<>();

        org.jsoup.nodes.Document html = Jsoup.parse(proxyUrlContent.getContent()
                , proxyUrlContent.getUrl());
        String body = html.body().text();
        if (body == null || "".equals(body)) return null;
        String[] proxyStrArr = body.split(" ");
        if (proxyStrArr == null || proxyStrArr.length < 1) return null;

        for (String proxyStr : proxyStrArr) {
            String[] proxyArr = proxyStr.split(":");
            if (proxyArr == null || proxyArr.length < 2) continue;

            String addr = proxyArr[0];
            int port;
            try {
                port = Integer.valueOf(proxyArr[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                continue;
            }

            RawProxy rawProxy = new RawProxy();
            rawProxy.setAddr(addr);
            rawProxy.setPort(port);
            rawProxy.setReferenceNum(0);

            rawProxies.add(rawProxy);
        }

        return rawProxies;
    }
}
