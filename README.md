## ProxySpider

ProxySpider 项目是为了维护一个稳定的代理池，为爬虫项目提供有效的代理。
项目分为：爬取代理、验证代理、获取代理

### 爬取代理

> 配置 (application.conf)

```
mongo {
  cluster="192.168.1.151:27017" # mongo集群
  db="proxy_full"               # 代理库
  url.col="proxy_url"           # 爬取 url 集合
  rawproxy.col="raw_proxy"      # 爬取原始的代理
  proxy.col="proxy"             # 验证后的代理
}
```

> 启动爬取代理进程

目前仅爬取了 xici 和 ip66 的代理，可以通过继承 cn.wangz.spider.proxy.task.SpiderOperate 类进行扩展。
首先需要向 mongo.url.col 表中加入爬取代理的 url (xici/ip66)，导入下面数据可直接使用，通过 "spider_service.sh start" 命令启动爬取进程。

```
{
    "url" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "date" : NumberLong(1542853682079)
}

{
    "url" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "date" : NumberLong(1542853934399)
}

{
    "refer" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/wn/1",
    "date" : NumberLong(1542853994599)
}

{
    "refer" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/wn/2",
    "date" : NumberLong(1542854059739)
}

{
    "refer" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/wn/3",
    "date" : NumberLong(1542854119915)
}

{
    "refer" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/wn/4",
    "date" : NumberLong(1542854180180)
}

{
    "refer" : "http://www.xicidaili.com/wn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/wn/5",
    "date" : NumberLong(1542854245348)
}

{
    "url" : "http://www.proxylists.net/http_highanon.txt",
    "type" : "text",
    "status" : 1,
    "num" : 1,
    "date" : NumberLong(1542854306068)
}

{
    "url" : "http://www.proxylists.net/http.txt",
    "type" : "text",
    "status" : 1,
    "num" : 1,
    "date" : NumberLong(1542854366694)
}

{
    "refer" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/nn/1",
    "date" : NumberLong(1542854426893)
}

{
    "refer" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/nn/2",
    "date" : NumberLong(1542854492054)
}

{
    "refer" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/nn/3",
    "date" : NumberLong(1542854552273)
}

{
    "refer" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/nn/4",
    "date" : NumberLong(1542854612607)
}

{
    "refer" : "http://www.xicidaili.com/nn/",
    "type" : "xici",
    "status" : 1,
    "num" : 1,
    "url" : "http://www.xicidaili.com/nn/5",
    "date" : NumberLong(1542854672738)
}

{
    "url" : "http://www.66ip.cn/mo.php?sxb=&tqsl=1000&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=http%3A%2F%2Fwww.66ip.cn%2F%3Fsxb%3D%26tqsl%3D1000%26ports%255B%255D2%3D%26ktip%3D%26sxa%3D%26radio%3Dradio%26submit%3D%25CC%25E1%2B%2B%25C8%25A1",
    "type" : "text",
    "status" : 1,
    "num" : 1,
    "date" : NumberLong(1542870812059)
}

{
    "url" : "http://www.66ip.cn/nmtq.php?getnum=300&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=1&api=66ip",
    "type" : "text",
    "status" : 0,
    "num" : 0,
    "date" : NumberLong(1542853743242)
}

{
    "url" : "http://www.89ip.cn/tqdl.html?api=1&num=1000&port=&address=&isp=",
    "type" : "text",
    "status" : 0,
    "num" : 0,
    "date" : NumberLong(1542853874099)
}
```

### 启动验证代理进程

在 cn.wangz.spider.proxy.job.ProxyValidateManager 中有两个参数：validateUrl（验证url）和 validateKeyword（验证keyword）需要对提供的代理服务进行定制化配置。
通过 "spider_service.sh start" 命令启动验证进程。

### 获取代理

在需要使用代理的爬虫服务中，引入 cn.wangz.spider.proxy.util.ProxyUtil 类。
通过 ProxyUtil.getProxy() 方法获取代理。代理失败通过 ProxyUtil.failProxy(Proxy proxy) 方法标记失败，失败3次后会删除代理。
ProxyUtil 还提供了其他的一些方法，具体可查看代码注释。 