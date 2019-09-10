package com.mistray.iot.common.bean;

import lombok.Data;

import java.util.Date;

/**
 * @author MistRay
 * @Project netty-server
 * @Package com.mistray.iot.common.bean
 * @create 2019年09月10日 12:29
 * @Desc
 */
@Data
public class RequestObject {

    private String method;
    private Date date;
}
