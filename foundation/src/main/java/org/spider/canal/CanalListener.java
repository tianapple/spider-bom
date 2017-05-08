package org.spider.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * canal处理监听接口
 * <p>
 * Created by tianapple on 2016/7/1.
 */
public interface CanalListener {
    /**
     * 数据更改处理
     *
     * @param database  数据库
     * @param tableName 变更表名
     * @param entry     更改记录
     */
    void process(String database, String tableName, CanalEntry.Entry entry) throws InvalidProtocolBufferException;
}
