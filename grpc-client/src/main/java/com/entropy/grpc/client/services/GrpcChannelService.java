package com.entropy.grpc.client.services;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import java.util.List;

public interface GrpcChannelService {
    Channel createChannel(String name, List<ClientInterceptor> interceptors);
}
