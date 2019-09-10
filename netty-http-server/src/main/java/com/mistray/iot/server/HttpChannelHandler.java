package com.mistray.iot.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mistray.iot.common.bean.RequestObject;
import com.mistray.iot.common.serializer.JSONSerializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author MistRay
 * @Project netty-server
 * @Package com.mistray.iot.server
 * @create 2019年09月10日 11:25
 * @Desc
 */
@Slf4j
@Sharable
@Component("HttpChannelHandler")
public class HttpChannelHandler extends SimpleChannelInboundHandler<HttpObject> {


    private static final String FAVICON_ICO = "/favicon.ico";
    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        RequestObject requestObject = new RequestObject();
        requestObject.setDate(new Date());
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            HttpHeaders headers = request.headers();
            String uri = request.uri();
            log.info("http uri: " + uri);
            if (uri.equals(FAVICON_ICO)) {
                return;
            }
            HttpMethod method = request.method();
            // Get请求
            if (method.equals(HttpMethod.GET)) {
                // 查询字符集
                QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
                doService(queryDecoder);
                requestObject.setMethod("get");
            } else if (method.equals(HttpMethod.POST)) {
                //POST请求,由于你需要从消息体中获取数据,因此有必要把msg转换成FullHttpRequest
                FullHttpRequest fullRequest = (FullHttpRequest) msg;
                //根据不同的Content_Type处理body数据
                dealWithContentType(fullRequest);
                requestObject.setMethod("post");
            }

            JSONSerializer jsonSerializer = new JSONSerializer();
            byte[] content = jsonSerializer.serialize(requestObject);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 简单处理常用几种 Content-Type 的 POST 内容
     *
     * @throws Exception
     */
    private void dealWithContentType(FullHttpRequest fullHttpRequest) throws Exception {
        String contentType = getContentType(fullHttpRequest.headers());
        //可以使用HttpJsonDecoder
        switch (contentType) {
            case "application/json": {
                String jsonStr = fullHttpRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
                JSONObject obj = JSON.parseObject(jsonStr);
                for (Map.Entry<String, Object> item : obj.entrySet()) {
                    log.info(item.getKey() + "=" + item.getValue().toString());
                }

                break;
            }
            case "application/x-www-form-urlencoded": {
                //方式一：使用 QueryStringDecoder
                String jsonStr = fullHttpRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
                QueryStringDecoder queryDecoder = new QueryStringDecoder(jsonStr, false);
                doService(queryDecoder);

                break;
            }
            case "multipart/form-data":
                //TODO 用于文件上传
                break;
            default:
                //do nothing...
                break;
        }
    }

    /**
     * 解码
     *
     * @param queryDecoder 解码器
     */
    private void doService(QueryStringDecoder queryDecoder) {
        Map<String, List<String>> uriAttributes = queryDecoder.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            for (String attrVal : attr.getValue()) {
                log.info(attr.getKey() + "=" + attrVal);
            }
        }
    }

    private String getContentType(HttpHeaders httpHeaders) {
        String typeStr = httpHeaders.get("Content-Type");
        String[] list = typeStr.split(";");
        return list[0];
    }
}
