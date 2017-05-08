package org.spider.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spider.util.StringUtils;

import java.util.List;

/**
 * canal客户端操作简单封装
 * <p>
 * Created by tianapple on 2016/7/1.
 */
public class CanalClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanalClient.class);

    private String zkAddress;
    private String ignoreTables;
    private String canalInstance;
    private int batchSize = 100;

    private CanalListener canalListener;
    private boolean running;

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public void setCanalInstance(String canalInstance) {
        this.canalInstance = canalInstance;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setIgnoreTables(String ignoreTables) {
        this.ignoreTables = ignoreTables;
    }

    public void setCanalListener(CanalListener canalListener) {
        this.canalListener = canalListener;
    }

    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        });
        thread.start();
        running = true;
    }

    private void process() {
        CanalConnector connector = CanalConnectors.newClusterConnector(zkAddress, canalInstance, "", "");
        while (running) {
            try {
                LOGGER.info("Connect zookeeper {}.{}, batchSize:{}, ignoreTables:{}", zkAddress, canalInstance, batchSize, ignoreTables);
                connector.connect();
                LOGGER.info("Ready subscribe....");
                connector.subscribe(""); //.*\..*
                LOGGER.info("Canal client start success.");
                while (running) {
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        Thread.sleep(2000);
                        continue;
                    }

                    LOGGER.info("message[batchId={}, size={}]", batchId, size);
                    try {
                        processEntity(message.getEntries());
                        connector.ack(batchId); // 提交确认
                    } catch (Exception e) {
                        connector.rollback(batchId);
                        LOGGER.error("process", e);
                        Thread.sleep(2000);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
                try {
                    Thread.sleep(5000);
                } catch (Exception inner) {
                    LOGGER.error("", inner);
                }
            } finally {
                try {
                    connector.disconnect();
                } catch (Exception inner) {
                    LOGGER.error("", inner);
                }
            }
        }
    }

    private void processEntity(List<CanalEntry.Entry> entries) throws InvalidProtocolBufferException {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            String database = entry.getHeader().getSchemaName().toLowerCase();
            String tableName = entry.getHeader().getTableName().toLowerCase();
            if (!StringUtils.isNullOrEmpty(ignoreTables) && ignoreTables.contains(tableName)) {
                return;  //忽略的表返回
            }
            canalListener.process(database, tableName, entry);
        }
    }

}
