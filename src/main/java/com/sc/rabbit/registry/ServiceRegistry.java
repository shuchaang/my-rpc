package com.sc.rabbit.registry;

import com.sc.rabbit.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author shuchang
 * Created on  2019-03-26
 */
@Slf4j
public class ServiceRegistry {
    private CountDownLatch latch = new CountDownLatch(1);
    private String address;


    public ServiceRegistry(String address){
        this.address = address;
    }


    public void register(String data){
        if(null!=data){
            ZooKeeper zk = getConnect();
            if(zk!=null){
                createNode(zk,data);
            }
        }
    }

    private ZooKeeper getConnect() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(address, Constant.ZK_TIMEOUT, new Watcher() {
                public void process(WatchedEvent event) {
                    if(event.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zk;
    }


    private void createNode(ZooKeeper zk,String data){
        byte[] bytes = data.getBytes();
        try {
            String path = zk.create(Constant.ZK_DATA_PATH,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("create Node error ");
        }
    }

}
