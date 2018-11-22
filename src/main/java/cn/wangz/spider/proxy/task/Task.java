package cn.wangz.spider.proxy.task;

/**
 * Created by hadoop on 2018/11/21.
 */
public interface Task {

    public void start() throws Exception;

    public void close();
}