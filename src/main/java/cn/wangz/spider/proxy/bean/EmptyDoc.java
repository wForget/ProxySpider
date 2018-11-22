package cn.wangz.spider.proxy.bean;
/**
 * 
 * @author yangqi   
 * @date 2015-8-13 下午3:30:33    
 * 空实体类; 用于同步队列
 */
public abstract class EmptyDoc {
	
	private boolean isEmpty = false;

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}
	
}
