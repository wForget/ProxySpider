//package cn.wangz.spider.proxy.task.operate;
//
//import cn.wangz.spider.proxy.bean.ProxyUrlContent;
//import cn.wangz.spider.proxy.task.SpiderOperate;
//import org.bson.Document;
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.select.Elements;
//
//import java.io.UnsupportedEncodingException;
//import java.net.Proxy;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * url: http://www.kuaidaili.com/free/inha/
// *      http://www.kuaidaili.com/free/intr/
// */
//public class KuaidailiProxyOperate implements SpiderOperate {
//
//    private Map cookies = null;
//
//    @Override
//    public ProxyUrlContent download(String url, Proxy proxy) {
//        try {
//            Connection connection = Jsoup.connect(url);
//            if (proxy != null) {
//                connection.proxy(proxy);
//                System.out.println(proxy.address());
//            }
//            if (cookies != null && !cookies.isEmpty()) {
//                connection.cookies(cookies);
//            }
//
//            connection.timeout(5000)
//                    .method(Connection.Method.GET)
//                    .header("Accept", "*/*")
//                    .followRedirects(true)	// 是否跟随跳转, 处理3开头的状态码
//                    .ignoreHttpErrors(true)	// 是否忽略网络错误, 处理5开头的状态码
//                    .ignoreContentType(true)	// 是否忽略类型, 处理图片、音频、视频等下载
//                    .header("Accept-Language", "zh-CN")
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
//
//            Connection.Response response = connection.execute();
//            String realUrl = response.url().toString();
//
//            int status = response.statusCode();
//            System.out.println(status);
////            if (status == 521) {
////                this.cookies = response.cookies();
////                String ydclearance = getYdclearanceCookie(response.bodyAsBytes());
////                this.cookies.put("_ydclearance", ydclearance);
////
////                return download(url, proxy);
////            }
//            if (status == 503) {
//                Thread.sleep(60000);
//                return download(url, proxy);
//            }
//
//            if (status != 404 && status != 403 && status < 500) {
//                byte[] content = response.bodyAsBytes();
//                if (content == null || content.length < 50) {
//                    return null;
//                }
//
//                ProxyUrlContent proxyUrlContent = new ProxyUrlContent();
//                proxyUrlContent.setStatusCode(status);
//                proxyUrlContent.setUrl(realUrl);
//                proxyUrlContent.setContent(content);
//
//                return proxyUrlContent;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private static Pattern funcPattern = Pattern.compile("(function.*)</script>", Pattern.CASE_INSENSITIVE);
//    private static Pattern funcNameAndParamPattern = Pattern.compile("setTimeout\\(\"(.*)\\((.*)\\)\", 200\\)", Pattern.CASE_INSENSITIVE);
//    private static Pattern valuePattern = Pattern.compile("document.cookie='_ydclearance=([^;]*);", Pattern.CASE_INSENSITIVE);
//    private String getYdclearanceCookie(byte[] content) {
//        String html = new String(content);
//        String func = null;
//        String funcName = null;
//        String param = null;
//
//        Matcher funcMatcher = funcPattern.matcher(html);
//        if (funcMatcher.find()) {
//            func = funcMatcher.group(1);
//            func = func.replaceAll("eval\\(\"qo=eval;qo\\(po\\);\"\\);", "return po;");
//        }
//        Matcher paramMatcher = funcNameAndParamPattern.matcher(html);
//        if (paramMatcher.find()) {
//            funcName = paramMatcher.group(1);
//            param = paramMatcher.group(2);
//        }
//        if (func == null || "".equals(func)
//                || funcName == null || "".equals(funcName)
//                || param == null || "".equals(param)) {
//            return null;
//        }
//
//        String ydclearanceStr = (String) JavaScriptUtil.eval(func, funcName, param);
//        Matcher valueMatcher = valuePattern.matcher(ydclearanceStr);
//        String value = null;
//        if (valueMatcher.find()) {
//            value = valueMatcher.group(1);
//        }
//
//        return value;
//    }
//
//    @Override
//    public List<Document> parse(ProxyUrlContent proxyUrlContent) {
//        int index = proxyUrlContent.getIndex();
//        switch (index) {
//            case 0:
//                return parsePageUrl(proxyUrlContent);
//            case 1:
//                return parseProxy(proxyUrlContent);
//            default:
//                break;
//        }
//
//        return null;
//    }
//
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
////                Elements pageElements = html.select("div#listnav a");
////                if (pageElements == null || pageElements.isEmpty()) {
////                    return null;
////                }
////                Element pageElement = pageElements.get(pageElements.size() - 1);
////                if (pageElement == null) {
////                    return null;
////                }
////                int page = Integer.valueOf(pageElement.text().trim());
//
//                int page = 50;
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
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private List<Document> parseProxy(ProxyUrlContent proxyUrlContent) {
//        List<Document> docList = null;
//
//        try {
//            org.jsoup.nodes.Document html = Jsoup.parse(new String(proxyUrlContent.getContent(), "utf-8")
//                    , proxyUrlContent.getUrl());
//            if (html != null) {
//                Elements trelements = html.select("table.table tbody tr");
//                if (trelements != null && trelements.size() > 1) {
//                    docList = new ArrayList<>();
//                    String refer = proxyUrlContent.getUrl();
//                    Integer type = proxyUrlContent.getType();
//                    int index = proxyUrlContent.getIndex() + 1;
//
//                    long date = System.currentTimeMillis();
//
//                    for (int i = 1; i < trelements.size(); i++) {
//                        Elements tdelements = trelements.get(i).select("td");
//                        String addr = tdelements.get(0).text();
//                        int port = Integer.valueOf(tdelements.get(1).text());
//
//                        if (addr != null && !"".equals(addr) && port > 0 && port < 65535) {
////                            if (!ProxyUtil.isValid(addr, port)) {
////                                continue;
////                            }
//
//                            Document doc = new Document();
//                            doc.append("addr", addr);
//                            doc.append("port", port);
//                            doc.append("isValid", true);
//                            doc.append("referenceNum", 0L);
//                            doc.append("date", date);
//
//                            docList.add(doc);
//                        }
//                    }
//                    return docList;
//                }
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//}
