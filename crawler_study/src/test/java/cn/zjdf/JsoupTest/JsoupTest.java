package cn.zjdf.JsoupTest;

import cn.zjdf.util.HttpUtil;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;

/**
 * @Classname JsoupTest
 * @Date 2020-08-03 21:37
 * @Created by Yujz
 * @Description jsoup使用测试
 */
public class JsoupTest {
    @Test
    public void testGetDocument() throws IOException {
        /**
         * @param: []
         * @return: void
         * @author: Yujz
         * @date: 2020-08-03 21:39
         * desc: 对网页源代码使用jsoup进行解析提取
         */
        //注意：虽然Jsoup可以直接对网站发起请求，但是功能有限，实际中还是需要使用HttpClient来进行网站的请求
        //Document htmlDoc = Jsoup.connect("http://www.itcast.com").get();
        String url = "http://www.itcast.com";
        Document htmlDoc = Jsoup.parse(HttpUtil.getHtml(url));
        //根据需要获取需要的数据
        String title = htmlDoc.getElementsByTag("title").first().text();
        System.out.println("网页解析的title信息：" + title);

    }

    @Test
    public void testOtherApi() {
        /**
         * @param: []
         * @return: void
         * @author: Yujz
         * @date: 2020-08-03 22:43
         * desc: 测试jsoup其他提取方法
         */
        String url = "http://www.itcast.com";
        Document htmlDoc = Jsoup.parse(HttpUtil.getHtml(url));
        //根据需要获取需要的数据
        String title = htmlDoc.getElementsByTag("title").first().text();
        System.out.println("根据网页的tag标签，获取网title信息：" + title);

    }

    @Test
    public void paserHtml() {
        String url = "http://www.itcast.com";
        Document htmlDoc = Jsoup.parse(HttpUtil.getHtml(url));
        //
        Elements subjects = htmlDoc.select(".ulon .a_gd");
        for (Element subject : subjects) {
            System.out.println(subject.text());
        }
    }
}
