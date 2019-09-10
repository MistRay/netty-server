package com.mistray.iot.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author MistRay
 * @Project netty-server
 * @Package com.mistray.iot.server
 * @create 2019年09月10日 11:16
 * @Desc
 */
@Component
@Slf4j
public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    @Qualifier("HttpChannelHandler")
    private HttpChannelHandler httpChannelHandler;


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
        pipeline.addLast(httpChannelHandler);
    }
}
