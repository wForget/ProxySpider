package cn.wangz.spider.proxy.bean;

import org.bson.Document;

/**
 * Created by hadoop on 2018/11/21.
 */
public interface UpdateDoc {

    Document getQueryDoc();

    Document getUpdateDoc();

}
