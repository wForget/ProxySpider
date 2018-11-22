package cn.wangz.spider.proxy.task;



import cn.wangz.spider.proxy.bean.ParseDoc;
import cn.wangz.spider.proxy.bean.ProxyUrlContent;

import java.net.Proxy;

/**
 * Created by hadoop on 2018/11/21.
 */
public interface SpiderOperate {
	
	/**
	 * @param url 链接
	 * @param proxy 代理; 可为空
	 * @return
	 * @throws Exception
	 */
	ProxyUrlContent download(String url, Proxy proxy);

	/**
	 * 解析页面
	 * @param proxyUrlContent
	 * @return
	 */
	ParseDoc parse(ProxyUrlContent proxyUrlContent);
}
