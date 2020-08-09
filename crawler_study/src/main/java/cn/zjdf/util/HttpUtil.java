package cn.zjdf.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Classname HttpUtil
 * @Date 2020-08-03 20:17
 * @Created by Yujz
 * @Description 编写httpclient工具类，方便后续直接使用该工具对网页内容进行爬取
 * 目标：后续可以直接使用类名，方法名（传入要爬取的url），返回爬取的结果字符串,工具类应该是让别人直接使用，不需要去new，所有
 * 可以把工具类设计成抽象类
 */
public abstract class HttpUtil {

    private static PoolingHttpClientConnectionManager cm = null; //连接池对象
    private static RequestConfig config = null;//请求配置对象
    private static List<String> userAgents = null;//浏览器头信息

    //静态代码块：在类初始化时候仅被调用一次
    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200); //设置连接池数
        config = RequestConfig.custom().setConnectTimeout(1000).build(); // 设置超时时间
        userAgents = new ArrayList<String>();//添加多种浏览器请求头信息
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:73.0) Gecko/20100101 Firefox/73.0");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
    }


    public static String getHtml(String url) {
        /**
         * @param: [url]
         * @return: java.lang.String
         * @author: Yujz
         * @date: 2020-08-03 20:28
         * desc: 静态方法，传入url，返回指定爬取页面的解析数据。
         */
        //不能使用简单入门的爬取代码，因为每次使用完HttpClient都会关闭，而是应该使用HttpClent提供的连接，这样
        //  当进行大数据量爬取的时候就不会因为过于频繁的开启关闭连接导致影响性能，而且连接池对象的创建不应该在方法中，
        //而应该在类被初始化的时候创建连接池对象（仅一次）
        //1.从cm连接池管理器中获取连接
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        //2.创建HttpGet对象并设置参数
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config); //设置准备好配置对象
        //随机获取一个浏览器请求头信息
        int index = new Random().nextInt(userAgents.size());//获取范围[0,size)的索引
        String userAgentStr = userAgents.get(index);
        httpGet.setHeader("User-Agent", userAgentStr);

        //3.发起请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            //4.获取响应内容，并返回
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; //如果返回为空，则有异常
    }

    public static void main(String[] args) {
        String url = "http://www.itcast.com";
        System.out.println(HttpUtil.getHtml(url));
    }
}
