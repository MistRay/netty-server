package com.mistray.iot.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;

/**
 * @author MistRay
 * @Project netty-server
 * @Package com.mistray.iot.server
 * @create 2019年09月10日 11:16
 * @Desc
 */
public class HttpInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        /**
         * 或者使用HttpRequestDecoder & HttpResponseEncoder
         */
        pipeline.addLast(new HttpServerCodec());
        /**
         * 在处理POST消息体时需要加上
         */
        pipeline.addLast(new HttpObjectAggregator(1024*1024));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        pipeline.addLast(new HttpChannelHandler());
    }
}
