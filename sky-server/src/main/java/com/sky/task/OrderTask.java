package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description 定时任务类，定时处理订单状态
 * @Author fjy
 * @Date 2024-03-16
 **/
@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;
    // @Autowired
    // OrderService orderService;
    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ? ")// 每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")// 每天凌晨一点触发一次
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中状态的订单：{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

    // @Scheduled(cron="0 * * * * ? ")
    // public void testPay(){
    //     log.info("支付成功！您有一笔新的订单！测试！！！");
    //     orderService.paySuccess("1710595772568");
    //     orderService.paySuccess("1710595836961");
    //     orderService.paySuccess("1710595872668");
    // }
}
