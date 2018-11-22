package cn.wangz.spider.proxy.job;


import cn.wangz.spider.proxy.server.MongoServer;
import cn.wangz.spider.proxy.task.SpiderOperateFactory;
import cn.wangz.spider.proxy.task.SpiderTask;
import cn.wangz.spider.proxy.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 爬取代理ip
 */
public class ProxySpiderManager implements SignalHandler {
    private Task task = null;

    private Logger logger = LoggerFactory.getLogger(ProxySpiderManager.class);

    public ProxySpiderManager() throws Exception {
        task = new SpiderTask();
    }

    @Override
    public void handle(Signal signal) {
        try {
            task.close();

            MongoServer.close();
        } catch (Exception e) {
            printErrorMsg(e);
        }
    }

    public void start() {
        try {
            task.start();
        } catch (Exception e) {
            printErrorMsg(e);
        }
    }

    //
    // 打印日志
    //
    private void printErrorMsg(Exception e) {
        e.printStackTrace();
        logger.error(e.getMessage());
    }

    public static void main(String[] args) throws Exception {
        ProxySpiderManager manager = new ProxySpiderManager();
        Signal.handle(new Signal("INT"), manager);  //kill -2
        //Signal.handle(new Signal("TERM"), manager);  //kill -15

        manager.start();
    }
}
