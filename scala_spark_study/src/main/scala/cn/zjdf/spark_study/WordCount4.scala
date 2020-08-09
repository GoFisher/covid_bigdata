package cn.zjdf.spark_study

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @Classname WordCount4
 * @Date 2020年8月5日 10:36:45
 * @Created by Yujz
 * @Description 使用StructedStreaming完成商品实时计算
 */
object WordCount4 {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir","C:\\hadoop-2.6.4")//设置winutils位置

    //1.准备StructuredStreaming执行环境 spark: SparkSession
    val spark: SparkSession = SparkSession.builder().appName("WordCount4").master("local[*]").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")

    //2.连接Kafka实时获取数据
    import spark.implicits._
    //DataFrame就是一个分布式的表
    val df: DataFrame = spark.readStream  //表示使用SparkSession读取流数据
      .format("kafka")//表示从Kafka读取
      .option("kafka.bootstrap.servers", "localhost:9092")//kafka集群地址,本地直接写localhost:9092
      .option("subscribe", "covid19")//订阅/消费哪个主题
      .load()//加载数据

    //3.处理计算实时数据
    //3.1获取value,将从Kafka消费的二进制的Value数据转为String类型
    //val ds: Dataset[一行行的商品] ,Dataset可以理解为分布式的表,相比于前面的DataFrame,增加了泛型而已
    val ds: Dataset[String] = df.selectExpr("CAST(value AS STRING)").as[String]
    //3.2将每一行商品按照" "进行切分并压平
    //val goodsDS: Dataset[一个个商品],
    // 注意:goodsDS: Dataset是一个分布式的表,现在里面只有一列,列名为默认的value,值为各个商品:
    //如:
    /* |value|
       ------
      |huawei|
      -------
      |huawei|
      -------
     */
    val goodsDS: Dataset[String] = ds.flatMap(_.split(" "))
    //3.3使用类SQL对数据进行统计
    val result: DataFrame = goodsDS.groupBy("value").count()//分组计数

    //4.输出结果
    result.writeStream//往外写结果
      .format("console")//写到控制台去
      .outputMode("complete")//输出模式为完整输出,就是数据全部输出
      //5.启动并等待结束
      .start()
      .awaitTermination()
  }


}
