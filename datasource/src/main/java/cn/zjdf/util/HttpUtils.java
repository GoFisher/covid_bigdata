package cn.zjdf.util;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author itcast
 * Date 2020/8/2 11:18
 * Desc 编写HttpClient工具类,方便后续直接使用该工具类对网页内容进行爬取
 * 注意:
 * 工具类应该是让别人直接使用类名.方法名即可调用(传入要爬取的url,返回爬取的结果字符串)
 * 不需要去new 工具类,所以可以把工具类设计为抽象类,java中的抽象类不运行new (或者提供私有构造)
 */
public abstract class HttpUtils {
    //声明一些需要用的的对象
    private static PoolingHttpClientConnectionManager cm = null;//连接池对象
    private static RequestConfig config = null;//请求配置对象
    private static List<String> userAgents = null;//存放浏览器头信息
    private static Random ran = null;//用来生成数据数的对象

    //静态代码块的特点是只会在该类初始化的时候被调用一次
    //那么如果将连接池对象的创建放在这里,就可以保证连接池对象只有一个
    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);//设置最大连接数
        config = RequestConfig.custom()
                .setConnectTimeout(10000)//设置连接超时时间
                .build();
        userAgents = new ArrayList<String>();
        //存放一些各种各样的浏览器头信息,方便后续爬虫随机模拟一个浏览器
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:73.0) Gecko/20100101 Firefox/73.0");
        userAgents.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        userAgents.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        ran = new Random();


    }


    public static String getHtml(String url){
        //不能简单的使用入门案例中的代码,每次使用完HttpClient都把它关闭,而是应该使用HttpClient提供的连接池对象
        //每次获取HttpClient对象的时候从连接池中获取,用完之后归还给连接池
        //这样当进行大数据量爬取的时候就不会因为过于频繁的开启关闭连接导致影响性能
        //但是连接池对象应该只创建一次,不能每次使用都创建,所以连接池对象的创建不应该在该方法中
        //而应该在该类被初始化的时候创建连接池对象(仅一次)
        //可以使用上面的static静态代码块初始化一些只需要被创建一次的对象
        //1.从cm连接池管理器中获取连接
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        //2.创建HttpGet对象并设置参数
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);//设置准备好的配置对象
        //随机获取一个浏览器请求头并设置
        int index = ran.nextInt(userAgents.size());//获取[0~size)范围内的随机数作为index索引去userAgents中去获取浏览器请求头
        String userAgentStr = userAgents.get(index);
        httpGet.setHeader("User-Agent",userAgentStr);//可以设置请求头模拟浏览器

        CloseableHttpResponse response = null;
        try {
            //3.发起请求
            response = httpClient.execute(httpGet);
            //4.获取响应内容并返回
            if(response.getStatusLine().getStatusCode() == 200) {
                String html = EntityUtils.toString(response.getEntity(), "UTF-8");
                return html;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //5.关闭资源
            try {
                response.close();
                //httpClient不用关闭,会在空闲被连接池回收
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;//如果代码走到这里说明出现异常了,直接返回null即可或者返回"";
    }

    //测试,注意:如果之前的@Test用不了可以使用main进行测试
    public static void main(String[] args) {
        String html = HttpUtils.getHtml("http://www.itcast.cn/");
        System.out.println("使用工具类爬取的内容如下:");
        System.out.println(html);
    }
}
