package com.sc.rabbit.registry;

import com.sc.rabbit.constant.Constant;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author shuchang
 * Created on  2019-03-26
 */
@Slf4j
public class ServiceDiscover {

    CountDownLatch latch = new CountDownLatch(1);
    private String address;
    private List<String> dataList=new ArrayList<>();


    public String discover(){
        String data=null;

        int size = dataList.size();

        if(size>0){
            if(size==1){
                return dataList.get(0);
            }else{
                return dataList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return data;
    }



    public ServiceDiscover(String address){
        this.address=address;
        ZooKeeper zooKeeper = getConnect();
        if(zooKeeper!=null){
            watchNode(zooKeeper);
        }
    }

    private void watchNode(final ZooKeeper zooKeeper) {
        try {
            List<String> children = zooKeeper.getChildren(Constant.ZK_REGISTRY_ROOT_PATH, new Watcher() {
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zooKeeper);
                    }
                }
            });

            List<String> dataList = new ArrayList<String>();
            for (String child : children) {
                byte[] data = zooKeeper.getData(Constant.ZK_REGISTRY_ROOT_PATH + "/" + child, false, null);
                dataList.add(new String(data, CharsetUtil.UTF_8));
            }
            this.dataList=dataList;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
}
