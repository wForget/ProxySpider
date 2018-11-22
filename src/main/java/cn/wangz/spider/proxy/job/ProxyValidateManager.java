package cn.wangz.spider.proxy.job;

import cn.wangz.spider.proxy.server.MongoServer;
import cn.wangz.spider.proxy.task.Task;
import cn.wangz.spider.proxy.task.VolidateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 根据网站 和 关键字 验证代理
 */
public class ProxyValidateManager implements SignalHandler {
    private Logger logger = LoggerFactory.getLogger(ProxyValidateManager.class);

    private Task task = null;

    public ProxyValidateManager(String validateUrl, String validateKeyword) throws Exception {
        task = new VolidateTask(validateUrl, validateKeyword);
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
        String validateUrl = "https://www.baidu.com";
        String validateKeyword = "baidu";

        ProxyValidateManager manager = new ProxyValidateManager(validateUrl, validateKeyword);
        Signal.handle(new Signal("INT"), manager);  //kill -2
        //Signal.handle(new Signal("TERM"), manager);  //kill -15

        manager.start();
    }
}
