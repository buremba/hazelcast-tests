package com.craftedbytes.client;

import com.hazelcast.core.Hazelcast;

/**
 * Created by buremba <Burak Emre Kabakcı> on 26/08/14 20:24.
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        Hazelcast.newHazelcastInstance();
    }
}
