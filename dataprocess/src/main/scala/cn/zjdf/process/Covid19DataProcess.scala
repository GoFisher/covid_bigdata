package cn.zjdf.process

import cn.zjdf.bean.{CovidBean, StatisticsDataBean}
import cn.zjdf.util.BaseJdbcSink
import com.alibaba.fastjson.JSON
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

import scala.collection.immutable.StringOps
import scala.collection.mutable

/**
 * Author Yujz
 * Date 2020年8月5日 11:39:24
 * Desc 使用StructuredStreaming实时消费Kafka中的疫情数据并做统计分析
 * 1.全国疫情信息汇总数据
 * 2.各省份疫情数据
 * 3.全国疫情趋势
 * 4.各省份境外输入统计
 * 5.指定城市的疫情信息-如北京
 */
object Covid19DataProcess {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "D:\\Software\\hadoop-2.6.4") //设置winutils位置

    //1.准备StructuredStreaming执行环境 spark: SparkSession
    val spark: SparkSession = SparkSession.builder().appName("Covid19DataProcess").master("local[*]").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    import spark.implicits._
    //2.连接Kafka实时获取数据
    //DataFrame就是一个分布式的表
    val df: DataFrame = spark.readStream //表示使用SparkSession读取流数据
      .format("kafka") //表示从Kafka读取
      .option("kafka.bootstrap.servers", "localhost:9092") //kafka集群地址,本地直接写localhost:9092
      .option("subscribe", "covid19") //订阅/消费哪个主题
      .load() //加载数据

    //去除value（也就是获取爬取到各个省份和城市的数据）
    val jsonStrDS: Dataset[String] = df.selectExpr("CAST(value as STRING)").as[String]
    jsonStrDS.writeStream.format("console")
      .option("truncate", value = false)
      .outputMode("append")
      .start()
    //      .awaitTermination()//注释，后面才会运行

    //3.数据处理-转换
    //jsonStr解析为Bean
    val covidBeanDS: Dataset[CovidBean] = jsonStrDS.map(jsonStr => {
      //表示将每一条jsonStr转为CovidBean
      JSON.parseObject(jsonStr, classOf[CovidBean])
    })

    //分离出一级省份(省份特区直辖市自治区),也就是statisticsData不为null的
    val provinceDS: Dataset[CovidBean] = covidBeanDS.filter(_.statisticsData != null)
    /*provinceDS.writeStream
      .format("console")
      .option("truncate",false)//false表示列太长的话不省略
      .outputMode("append")//注意:append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      .start()
      .awaitTermination()*/
    //注掉改行,后面的才会运行

    //分离出二级城市(省份(省份特区直辖市自治区)下面的城市/区)也就是statisticsData为null的
    val cityDS: Dataset[CovidBean] = covidBeanDS.filter(_.statisticsData == null)
    /*cityDS.writeStream
      .format("console")
      .option("truncate",false)//false表示列太长的话不省略
      .outputMode("append")//注意:append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      .start()
      .awaitTermination()*/

    //分离出每一天的疫情数据[[2020-01-20,2020-01-21,2020-01-22....],[2020-01-20,2020-01-21,2020-01-22....],...]
    import scala.collection.JavaConversions._

    val statisticsDataBeanDS: Dataset[StatisticsDataBean] = provinceDS.flatMap(province => {
      val jsonStr: StringOps = province.statisticsData
      // {"code":"success","data":[{"confirmedCount":2,"confirmedIncr":2,"curedCount":0,"curedIncr":0,"current....
      //要先取出data,data后面才是[]json数组
      val jsonArrayStr: String = JSON.parseObject(jsonStr).getString("data")
      //表示将每一天的统计信息都封装成一个StatisticsDataBean,那么有很多天就是List<StatisticsDataBean>
      //而直接返回的是Java的List,需要转换为Scala的List,需要导入一个隐式转换(导包)
      val list: mutable.Buffer[StatisticsDataBean] = JSON.parseArray(jsonArrayStr, classOf[StatisticsDataBean])
      list.map(s => {
        s.provinceShortName = province.provinceShortName
        s.locationId = province.locationId
        s
      })
    })
    //    statisticsDataBeanDS.writeStream
    //      .format("console")
    //      .option("truncate",value = false)//false表示列太长的话不省略
    //      .outputMode("append")//注意:append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
    //      .start()
    //      .awaitTermination()

    //4.指标统计
    import org.apache.spark.sql.functions._
    // 4.1.全国疫情信息汇总数据-按照时间分组,统计每一天的全国疫情数据汇总信息(可以从一级省份数据中按时间分组聚合获得当天的结果)
    val result1: DataFrame = provinceDS.groupBy("datetime")
      .agg( //agg表示聚合
        // '字符串,表示将字符串转为列对象或者使用$"字符串"也是将字符串转为列对象, as表示起别名
        sum('currentConfirmedCount) as "currentConfirmedCount", //现存确诊
        sum('confirmedCount) as "confirmedCount", //累记确诊
        sum('suspectedCount) as "suspectedCount", //现存疑似
        sum('curedCount) as "curedCount", //累记治愈
        sum('deadCount) as "deadCount" //累记死亡
      )

    // 4.2.各省份疫情数据
    val result2: DataFrame = provinceDS.select(
      'datetime,
      'locationId,
      'provinceShortName,
      'currentConfirmedCount,
      'confirmedCount,
      'suspectedCount,
      'curedCount,
      'deadCount
    )


    // 4.3.全国每一天的疫情趋势
    val result3: DataFrame = statisticsDataBeanDS.groupBy('dateId)
      .agg(
        // '字符串,表示将字符串转为列对象或者使用$"字符串"也是将字符串转为列对象, as表示起别名
        sum('confirmedIncr) as "confirmedIncr", //新增确诊
        sum('confirmedCount) as "confirmedCount", //累加确诊
        sum('suspectedCount) as "suspectedCount", //现存疑似
        sum('curedCount) as "curedCount", //累加治愈
        sum('deadCount) as "deadCount" //累加死亡
      )

    // 4.4.各个省份境外输入统计
    val result4: DataFrame = cityDS.filter(_.cityName.contains("境外输入"))
      .groupBy('datetime, 'provinceShortName, 'pid)
      .agg(sum('confirmedCount) as "confirmedCount")

    // 4.5.指定城市的疫情数据-如北京(可选),北京是直辖市(一级省份),下面有很多区
    //我们要查北京的疫情信息应该是从cityDS(二级城市/直辖市的区)中过滤出省份短名为北京的
    val result5: DataFrame = cityDS.filter(_.provinceShortName.equals("北京"))
      .select(
        'datetime,
        'locationId,
        'provinceShortName, //北京
        'cityName, //北京下的区的名称
        'currentConfirmedCount,
        'confirmedCount,
        'suspectedCount,
        'curedCount,
        'deadCount
      )

    //5.输出结果-先到控制台再到MySQL(上线的时候只需要保存到MySQL)
    /*result1.writeStream
      .format("console")
      .option("truncate",false)//false表示列太长的话不省略
      //注意:
      // append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      // complete支持聚合
      .outputMode("complete")
      .start()
     .awaitTermination()*/

    /*result2.writeStream
      .format("console")
      .option("truncate",false)//false表示列太长的话不省略
      //注意:
      // append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      // complete支持聚合
      .outputMode("append")
      .start()
      .awaitTermination()*/

    /*result3.writeStream
      .format("console")
      .option("truncate", false) //false表示列太长的话不省略
      //注意:
      // append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      // complete支持聚合
      .outputMode("complete")
      .start()
      .awaitTermination()*/

    /*result4.writeStream
      .format("console")
      .option("truncate", false) //false表示列太长的话不省略
      //注意:
      // append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      // complete支持聚合
      .outputMode("complete")
      .start()
      .awaitTermination()*/

    result5.writeStream
      .format("console")
      .option("truncate", value = false) //false表示列太长的话不省略
      //注意:
      // append追加模式表示会输出新增的数据,适合简单查询(也就是没有聚合的)
      // complete支持聚合
      .outputMode("append")
      .start()
    //      .awaitTermination()

    //下节课将数据输出到MySQL--需要等待时间挺长
    result1.writeStream
      //使用自定义的BaseJdbcSink里面已经写好了open和close方法,只需要重写realProcess即可
      .foreach(new BaseJdbcSink("REPLACE INTO `covid19_1` (`datetime`, `currentConfirmedCount`, `confirmedCount`, `suspectedCount`, `curedCount`, `deadCount`) VALUES (?, ?, ?, ?, ?, ?);") {
        override def realProcess(sql: String, row: Row): Unit = {
          val datetime: String = row.getAs[String]("datetime")
          val currentConfirmedCount: Long = row.getAs[Long]("currentConfirmedCount")
          val confirmedCount: Long = row.getAs[Long]("confirmedCount")
          val suspectedCount: Long = row.getAs[Long]("suspectedCount")
          val curedCount: Long = row.getAs[Long]("curedCount")
          val deadCount: Long = row.getAs[Long]("deadCount")
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)
          ps.setLong(2,currentConfirmedCount)
          ps.setLong(3,confirmedCount)
          ps.setLong(4,suspectedCount)
          ps.setLong(5,curedCount)
          ps.setLong(6,deadCount)
          ps.executeUpdate()
        }
      }).outputMode("complete")
      .start()
    //.awaitTermination()

    result2.writeStream
      .foreach(new BaseJdbcSink("REPLACE INTO `covid19_2` (`datetime`, `locationId`, `provinceShortName`, `currentConfirmedCount`, `confirmedCount`, `suspectedCount`, `curedCount`, `deadCount`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);") {
        override def realProcess(sql: String, value: Row): Unit = {//注意row的索引从0开始
          val datetime: String = value.getAs[String]("datetime")
          val locationId: Int = value.getAs[Int]("locationId")
          val provinceShortName: String = value.getAs[String]("provinceShortName")
          val currentConfirmedCount: Int = value.getAs[Int]("currentConfirmedCount")
          val confirmedCount: Int = value.getAs[Int]("confirmedCount")
          val suspectedCount: Int = value.getAs[Int]("suspectedCount")
          val curedCount: Int = value.getAs[Int]("curedCount")
          val deadCount: Int = value.getAs[Int]("deadCount")
          //REPLACE表示如果有值则替换,如果没有值则新增(本质上是如果有值则删除再新增,如果没有值则直接新增)
          //注意:在创建表的时候需要指定唯一索引,或者联合主键来判断有没有这个值
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)//jdbc的索引从1开始
          ps.setInt(2,locationId)
          ps.setString(3,provinceShortName)
          ps.setInt(4,currentConfirmedCount)
          ps.setInt(5,confirmedCount)
          ps.setInt(6,suspectedCount)
          ps.setInt(7,curedCount)
          ps.setInt(8,deadCount)
          ps.executeUpdate()
        }
      }).outputMode("append")
      .start()

    result3.writeStream
      .foreach(new BaseJdbcSink("REPLACE INTO `covid19_3` (`dateId`, `confirmedIncr`, `confirmedCount`, `suspectedCount`, `curedCount`, `deadCount`) VALUES (?, ?, ?, ?, ?, ?);") {
        override def realProcess(sql: String, value: Row): Unit = {//注意row的索引从0开始
          val dateId: String = value.getAs[String]("dateId")
          val confirmedIncr: Long = value.getAs[Long]("confirmedIncr")
          val confirmedCount: Long = value.getAs[Long]("confirmedCount")
          val suspectedCount: Long = value.getAs[Long]("suspectedCount")
          val curedCount: Long = value.getAs[Long]("curedCount")
          val deadCount: Long = value.getAs[Long]("deadCount")
          ps = conn.prepareStatement(sql)
          ps.setString(1,dateId)//jdbc的索引从1开始
          ps.setLong(2,confirmedIncr)
          ps.setLong(3,confirmedCount)
          ps.setLong(4,suspectedCount)
          ps.setLong(5,curedCount)
          ps.setLong(6,deadCount)
          ps.executeUpdate()
        }
      }).outputMode("complete")
      .start()

    result4.writeStream
      .foreach(new BaseJdbcSink("REPLACE INTO `covid19_4` (`datetime`, `provinceShortName`, `pid`, `confirmedCount`) VALUES (?, ?, ?, ?);") {
        override def realProcess(sql: String, value: Row): Unit = {//注意row的索引从0开始
          val datetime: String = value.getAs[String]("datetime")
          val provinceShortName: String = value.getAs[String]("provinceShortName")
          val pid: Int = value.getAs[Int]("pid")
          val confirmedCount: Long = value.getAs[Long]("confirmedCount")
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)//jdbc的索引从1开始
          ps.setString(2,provinceShortName)
          ps.setInt(3,pid)
          ps.setLong(4,confirmedCount)
          ps.executeUpdate()
        }
      }).outputMode("complete")
      .start()

    result5.writeStream
      .foreach(new BaseJdbcSink("REPLACE INTO `covid19_5` (`datetime`, `locationId`, `provinceShortName`, `cityName`,`currentConfirmedCount`, `confirmedCount`, `suspectedCount`, `curedCount`, `deadCount`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);") {
        override def realProcess(sql: String, value: Row): Unit = {//注意row的索引从0开始
          val datetime: String = value.getAs[String]("datetime")
          val locationId: Int = value.getAs[Int]("locationId")
          val provinceShortName: String = value.getAs[String]("provinceShortName")
          val cityName: String = value.getAs[String]("cityName")
          val currentConfirmedCount: Int = value.getAs[Int]("currentConfirmedCount")
          val confirmedCount: Int = value.getAs[Int]("confirmedCount")
          val suspectedCount: Int = value.getAs[Int]("suspectedCount")
          val curedCount: Int = value.getAs[Int]("curedCount")
          val deadCount: Int = value.getAs[Int]("deadCount")
          //REPLACE表示如果有值则替换,如果没有值则新增(本质上是如果有值则删除再新增,如果没有值则直接新增)
          //注意:在创建表的时候需要指定唯一索引,或者联合主键来判断有没有这个值
          ps = conn.prepareStatement(sql)
          ps.setString(1,datetime)//jdbc的索引从1开始
          ps.setInt(2,locationId)
          ps.setString(3,provinceShortName)
          ps.setString(4,cityName)
          ps.setInt(5,currentConfirmedCount)
          ps.setInt(6,confirmedCount)
          ps.setInt(7,suspectedCount)
          ps.setInt(8,curedCount)
          ps.setInt(9,deadCount)
          ps.executeUpdate()
        }
      }).outputMode("append")
      //6.开启并等待结束
      .start()
      .awaitTermination()

  }
}
