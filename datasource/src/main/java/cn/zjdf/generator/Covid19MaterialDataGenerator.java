package cn.zjdf.generator;


import cn.zjdf.bean.MaterialBean;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Author Yujz
 * Date 2020年8月4日 21:01:00
 * Desc 新冠疫情物资数据模拟生成程序
 */
@Component
public class Covid19MaterialDataGenerator {

    @Autowired//表示由SpringBoot注入该对象给该变量
    private KafkaTemplate<Integer,String> kafkaTemplate;

    //准备一些数组来存放物资名称
    private String[] wzmc = new String[]{"N95口罩/个", "医用外科口罩/个", "84消毒液/瓶", "电子体温计/个", "一次性橡胶手套/副", "防护目镜/副", "医用防护服/套"};

    //准备一些数组用来存放物资来源
    private String[] wzly = new String[]{"采购", "下拨", "捐赠", "消耗", "需求"};


    //定时生成疫情物资数据方法
    //@Scheduled(initialDelay = 1000, fixedDelay = 10000) //启动1秒后，每隔10秒启动一次
    public void generator() {
        System.out.println("该方法启动1s后，每10s启动一次！");
        Random ran = new Random();
        for (int i = 0; i < 10; i++) {
            String name = wzmc[ran.nextInt(wzmc.length)];// 随机从wzmc数组中获取一个物资名称
            String from = wzly[ran.nextInt(wzly.length)];// 随机从wzly数组中获取一个物资来源
            Integer count = ran.nextInt(1000);//随机生产一个物资数量
            MaterialBean materialBean = new MaterialBean(name, from, count);
            String jsonStr = JSON.toJSONString(materialBean);
            System.out.println(jsonStr);
            kafkaTemplate.send("covid19_wz", ran.nextInt(3), jsonStr);
        }
    }
}
