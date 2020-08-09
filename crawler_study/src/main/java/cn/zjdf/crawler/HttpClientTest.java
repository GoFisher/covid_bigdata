package cn.zjdf.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * @Classname HttpClientTest
 * @Date 2020-08-03 13:32
 * @Created by Yujz
 * @Description TODO
 */
public class HttpClientTest {
    @Test //单元测试方法，可以直接运行，类型main方法
    public void testGet() {
        /**
         * @param: []
         * @return: void
         * @author: Yujz
         * @date: 2020-08-03 13:45
         * desc: 演示使用HttpClient实现简单网络爬取
         */
        //1.创建HttpClent客户端对象，相当于创建一个浏览器
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //2.创建HttpGet请求对象并设置相关请求配置，相当于浏览器中创建一个tab页面
        String url = "http://www.baidu.com";
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:50.0) Gecko/20100101 Firefox/50.0");
        CloseableHttpResponse response = null;

        try {
            //3.发起请求并返回请求结果对象
            response = httpClient.execute(httpGet);

            //4.解析响应放回的对象数据
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity(); //获取响应实体
                String html = EntityUtils.toString(entity, "UTF-8");
                System.out.println("爬取网页内容：");
                System.out.println(html);
            } else {
                System.out.println("网页响应异常！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                //5.关闭资源
                response.close(); //关闭标签页
                httpClient.close(); //关闭浏览器
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

