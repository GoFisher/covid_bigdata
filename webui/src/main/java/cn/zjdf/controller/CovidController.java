package cn.zjdf.controller;

import cn.zjdf.bean.Result;
import cn.zjdf.mapper.CovidMapper;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 这是一个用来接收浏览器请求的控制类
 */
@RestController//表示该类是由SpringBoot管理,并且返回Json数据格式
@RequestMapping("covid")//表示接收covid/.....请求
public class CovidController {

    @Autowired//表示由SpringBoot注入该对象
    private CovidMapper mapper;

    //1.全国疫情汇总信息
    //编写一个方法能够接收如下请求,并返回全国疫情汇总信息
    //http://localhost:8080/covid/getNationalData
    @RequestMapping("getNationalData")
    public Result getNationalData(){
        System.out.println("能够接收到前端发送的请求,后续调用mapper查询出全国疫情数据返回即可");
        //String datetime = "2020-08-06";
        String datetime = FastDateFormat.getInstance("yyyy-MM-dd").format(System.currentTimeMillis());
        List<Map<String,Object>> list = mapper.getNationalData(datetime);
        Map<String, Object> data = list.get(0);//该data就是从数据库中查询出来的满足条件的数据
        Result result = Result.success(data);
        return result;
        //注意:前端要的是json格式,这里直接把map放到result中给前端就行了
        //是因为:类上加了@RestController = @Controller + @ResponseBody
        //@Controller表示该类会被SpringBoot管理并可以接收前端请求(通过@RequestMapping指定)
        //@ResponseBody加在了类上,表示返回的是json(自动转)
        //这些是SpringMVC/SpringBoot里面的功能
    }


    //2.全国疫情地图
    //编写方法能够接收如下请求,并返回全国各个省份疫情数据信息
    //http://localhost:8080/covid/getNationalMapData
    @RequestMapping("getNationalMapData")
    public Result getNationalMapData(){
        String datetime = FastDateFormat.getInstance("yyyy-MM-dd").format(System.currentTimeMillis());
        List<Map<String,Object>> list = mapper.getNationalMapData(datetime);
        Result result = Result.success(list);
        return result;
    }

    //3.全国疫情趋势
    //编写方法能够接收如下请求,并返回全国疫情趋势时间折线图数据
    //http://localhost:8080/covid/getCovidTimeData
    @RequestMapping("getCovidTimeData")
    public Result getCovidTimeData(){
        List<Map<String,Object>> list = mapper.getCovidTimeData();
        Result result = Result.success(list);
        return result;
    }


    //4.境外输入统计
    //编写方法能够接收如下请求,并返回境外输入统计数据
    //http://localhost:8080/covid/getCovidImportData
    @RequestMapping("getCovidImportData")
    public Result getCovidImportData(){
        String datetime = FastDateFormat.getInstance("yyyy-MM-dd").format(System.currentTimeMillis());
        List<Map<String,Object>> list = mapper.getCovidImportData(datetime);
        Result result = Result.success(list);
        return result;
    }

    //5.物资数据统计
    //编写方法能够接收如下请求,并返回物资数据统计信息
    //http://localhost:8080/covid/getCovidWz
    @RequestMapping("getCovidWz")
    public Result getCovidWz(){
        List<Map<String,Object>> list = mapper.getCovidWz();
        Result result = Result.success(list);
        return result;
    }

}