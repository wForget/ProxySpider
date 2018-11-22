package cn.wangz.spider.proxy.bean;

/**
 * 
 * @author yangqi
 * @date 2016-3-16 上午11:47:13 下载数据实体类
 */
public class ProxyUrlContent extends EmptyDoc {

	private String url = ""; // 链接
	private String refer = "";
	private int statusCode = 200; // 状态码
	private String content = null; // 下载数据
	private String type = "";
	private int index = -1;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRefer() {
		return refer;
	}

	public void setRefer(String refer) {
		this.refer = refer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
