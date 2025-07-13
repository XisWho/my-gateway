package com.xw.gateway.etcd.api;

public interface WatcherListener {

    void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception;

}
