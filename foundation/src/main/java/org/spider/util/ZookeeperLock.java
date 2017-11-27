package org.spider.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 基于zookeeper的分布式锁
 * 用于每组集群服务只能启动一个服务做一些工作，当这个服务挂掉或下线，集群中其他的服务可以快速接替并继续运行
 * <p>
 * Created by tianapple on 2017/11/24.
 */
public class ZookeeperLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperLock.class);
    private static final String Lock_Root_Path = "/dis_lock";
    private static final String Lock_Path = "/dis_lock/lock";

    private String appName = "";
    private String zkAddress = "";
    private int timeout = 3000;

    public void setAppName(String appName) {
        this.appName = "/" + appName;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private String getLockRootPath() {
        return appName + Lock_Root_Path;
    }

    private String getLockPath() {
        return appName + Lock_Path;
    }

    //空Watcher，为了避免错误
    private final Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent watchedEvent) {
//            LOGGER.info("{}", watchedEvent);
        }
    };

    //创建Watcher，监控lockPath的前一个节点
    private final Watcher waitWatcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            // 创建的锁目录只有删除事件
            LOGGER.info("Received {} event, node path is {}", event.getType(), event.getPath());
            synchronized (this) {
                notifyAll(); //通知所有等待的线程
            }
        }
    };

    //获取一个zk客户端
    private volatile ZooKeeper zkClient = null;
    private ZooKeeper getZkClient() {
        if (zkClient == null) {
            try {
                synchronized (this) {
                    if (zkClient == null) {
                        LOGGER.info("connect zookeeper: {}", zkAddress);
                        zkClient = new ZooKeeper(zkAddress, timeout, watcher);
                        LOGGER.info("connect success {}!", zkClient);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("zk:{} create error:{}", zkAddress, e);
            }
        }
        return zkClient;
    }

    /**
     * 获取锁：实际上是创建线程目录，并判断线程目录序号是否最小
     */
    public String getLock() {
        if (StringUtils.isNullOrEmpty(appName)) {
            throw new NullPointerException("appName");
        }
        if (StringUtils.isNullOrEmpty(zkAddress)) {
            throw new NullPointerException("zkAddress");
        }

        try {
            //1.判断服务目录是否存在，不存在则创建
            ZooKeeper zkClient = getZkClient();
            Stat stat = zkClient.exists(appName, null);
            if (stat == null) {
                zkClient.create(appName, appName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); //Ids.OPEN_ACL_UNSAFE表示所有权限
            }
            //2.判断锁跟目录是否存在，不存在则创建
            String lockRoot = getLockRootPath();
            stat = zkClient.exists(lockRoot, null);
            if (stat == null) {
                zkClient.create(lockRoot, lockRoot.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            //3.创建当前锁目录
            String path = getLockPath();
            String lockPath = zkClient.create(path, path.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create lock path: {}", lockPath);
            //4.尝试获取锁
            tryLock(lockPath);
            return lockPath;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    private boolean tryLock(String lockPath) throws KeeperException, InterruptedException {
        // 获取ROOT_LOCK_PATH下所有的子节点，并按照节点序号排序
        String lockRootPath = getLockRootPath();
        List<String> allLockNames = getZkClient().getChildren(lockRootPath, false);
        Collections.sort(allLockNames);

        String curLockName = lockPath.substring(lockRootPath.length() + 1);
        int index = allLockNames.indexOf(curLockName); //返回在数组目录中的索引，如果是0则表示获取到了锁
        if (index == 0) {
            LOGGER.info("{} get lock path: {}", Thread.currentThread().getName(), lockPath);
            return true;
        } else {
            String preLockName = allLockNames.get(index - 1);
            preLockName = lockRootPath + "/" + preLockName;
            // 查询前一个目录是否存在，并且注册目录事件监听器，监听一次事件后即删除
            Stat state = getZkClient().exists(preLockName, waitWatcher);
            if (state == null) {
                return tryLock(lockPath);
            } else {
                LOGGER.info("{} wait lock for: {}", Thread.currentThread().getName(), preLockName);
                synchronized (waitWatcher) {
                    waitWatcher.wait(); // 等待目录删除事件唤醒
                }
                return tryLock(lockPath);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ZookeeperLock zkLock = new ZookeeperLock();
        zkLock.setZkAddress("10.88.5.35:2181");
        zkLock.setAppName("ibs");
        zkLock.setTimeout(30000);

        String lock = zkLock.getLock();
        System.in.read();
    }
}
