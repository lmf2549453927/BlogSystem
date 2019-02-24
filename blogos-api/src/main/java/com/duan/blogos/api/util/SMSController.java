package com.duan.blogos.api.util;

import com.alibaba.dubbo.config.annotation.Reference;
import com.duan.blogos.api.BaseCheckController;
import com.duan.blogos.service.common.restful.ResultModel;
import com.duan.blogos.service.SmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2018/2/18.
 *
 * @author DuanJiaNing
 */
@RestController
@RequestMapping("/sms")
public class SMSController extends BaseCheckController {

    @Reference
    private SmsService smsService;

    /**
     * 向指定号码发送短信
     */
    @PostMapping
    public ResultModel send(
            @RequestParam("phone") String phone,
            @RequestParam("content") String content) {

        return smsService.sendSmsTo(content, phone);
    }
}
