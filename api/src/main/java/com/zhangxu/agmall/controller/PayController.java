package com.zhangxu.agmall.controller;

import com.github.wxpay.sdk.WXPayUtil;
import com.zhangxu.agmall.service.OrderService;
import com.zhangxu.agmall.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private OrderService orderService;

    /**
     * 回调接口:当用户支付成功之后，微信支付平台就会请求这个接口，将支付状态的数据传递过来
     */
    @RequestMapping("/wxCallback")
    public String paySuccess(HttpServletRequest request) throws Exception {
        System.out.println("--------------------callback");
        // 1.接收微信支付平台传递的数据（使用request的输入流接收）
        ServletInputStream is = request.getInputStream();
        byte[] bs = new byte[1024];
        int len = -1;
        StringBuilder builder = new StringBuilder();
        while((len = is.read(bs))!=-1){
            builder.append(new String(bs,0,len));
        }
        String s = builder.toString();
        //使用帮助类将xml接口的字符串装换成map
        Map<String, String> map = WXPayUtil.xmlToMap(s);

        if(map!=null && "success".equalsIgnoreCase(map.get("result_code"))){
            //支付成功
            //2.修改订单状态为“待发货/已支付”
            String orderId = map.get("out_trade_no");
            int i = orderService.updateOrderStatus(orderId, "2");
            System.out.println("--orderId:"+orderId);
            //3.通过websocket连接，向前端推送消息
            System.out.println("微信调用回调接口了，支付成功");
            WebSocketServer.sendMsg(orderId,"1");
            System.out.println("通过webcocket向前端发送orderId，订单编号，和msg 1");
            //4.响应微信支付平台
            if(i>0){
                HashMap<String,String> resp = new HashMap<>();
                resp.put("return_code","success");
                resp.put("return_msg","OK");
                resp.put("appid",map.get("appid"));
                resp.put("result_code","success");
                return WXPayUtil.mapToXml(resp);
            }
        }
        return null;
    }
    @RequestMapping("/cancelSuccess")
    public String ordersCancelSuccess(HttpServletRequest request) throws Exception {
        System.out.println("--------------------callback");
        // 1.接收微信支付平台传递的数据（使用request的输入流接收）
        ServletInputStream is = request.getInputStream();
        byte[] bs = new byte[1024];
        int len = -1;
        StringBuilder builder = new StringBuilder();
        while((len = is.read(bs))!=-1){
            builder.append(new String(bs,0,len));
        }
        String s = builder.toString();
        //使用帮助类将xml接口的字符串装换成map
        Map<String, String> map = WXPayUtil.xmlToMap(s);

        if(map!=null && "success".equalsIgnoreCase(map.get("result_code"))){
            //支付成功
            //2.修改订单状态为“待发货/已支付”
            String orderId = map.get("out_trade_no");
            int i = orderService.updateOrderStatus(orderId, "2");
            System.out.println("--orderId:"+orderId);
            //3.通过websocket连接，向前端推送消息
            WebSocketServer.sendMsg(orderId,"1");

            //4.响应微信支付平台
            if(i>0){
                HashMap<String,String> resp = new HashMap<>();
                resp.put("return_code","success");
                resp.put("return_msg","OK");
                resp.put("appid",map.get("appid"));
                resp.put("result_code","success");
                return WXPayUtil.mapToXml(resp);
            }
        }
        return null;
    }

}
