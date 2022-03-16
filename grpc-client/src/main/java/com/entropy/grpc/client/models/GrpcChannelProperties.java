package com.entropy.grpc.client.models;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class GrpcChannelProperties {

    public static final GrpcChannelProperties DEFAULT_CHANNEL = new GrpcChannelProperties();

    private final List<String> hosts = new ArrayList<>();
    private final List<Integer> ports = new ArrayList<>();


    public List<String> getHosts() {
        return hosts;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public boolean isPlaintext() {
        return true;
    }

    public boolean isEnableKeepAlive() {
        return false;
    }

    public boolean isKeepAliveWithoutCalls() {
        return false;
    }

    public long getKeepAliveTime() {
        return 180;
    }

    public long getKeepAliveTimeout() {
        return 20;
    }
}
