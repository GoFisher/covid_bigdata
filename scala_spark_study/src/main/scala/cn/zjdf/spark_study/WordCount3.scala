package cn.zjdf.spark_study

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @Classname WordCount3
 * @Date 2020年8月5日 10:09:39
 * @Created by Yujz
 * @Description 使用sparkStreaming完成商品数量统计,有状态的商品计算，叠加历史数据，updateStateByKey
 * 但是状态维护需要自己手动编写代码进行维护，比较麻烦，并且API不支持SQL/类SQL,所有Spark在官方最新版本中
 * 2.0推出
 */
object WordCount3 {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir","c:\\hadoop-2.6.4")//设置winutils位置

    //1.准备SparkStreaming执行环境
    val conf: SparkConf = new SparkConf().setAppName("WordCount2").setMaster("local[*]")
    val context: SparkContext = new SparkContext(conf)
    val ssc: StreamingContext = new StreamingContext(context, Seconds(5))
    //Second（5）表示：每个5s对源源不断的数据进行处理

    //设置CheckPoint目录，用来保存历史状态结果，有状态计算时需要设置，该目录会自动创建
    ssc.checkpoint("./ckpdr")

    //2.连接kafka实时获取数据
    val kafkaParams: Map[String, Object] = Map[String, Object](
      //kafka集群地址，如果连接的是windows，写localhost：9092即可
      "bootstrap.servers" -> "localhost:9092", //kafka集群地址
      "key.deserializer" -> classOf[StringDeserializer], //key的反序列化类型
      "value.deserializer" -> classOf[StringDeserializer], //value的反序列化类型
      //消费发给Kafka需要经过网络传输,而经过网络传输都需要进行序列化,即消息发给kafka需要序列化,那么从kafka消费完就得反序列化
      "group.id" -> "SparkKafka", //消费者组名称
      //earliest:当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
      //latest:当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
      //none:当各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
      //这里配置latest自动重置偏移量为最新的偏移量,即如果有偏移量从偏移量位置开始消费,没有偏移量从新来的数据开始消费
      "auto.offset.reset" -> "latest",
      //使用手动提交offset
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )
    val topics = Array("covid19")//订阅哪个主题
    //2.2真正的连接Kafka获取数据
    //kafkaDStream: InputDStream[ConsumerRecord[String, String]]
    //kafkaDStream就是从Kafka中实时消费到的数据,DStream可以理解为时间上连续的RDD组成的实时的分布式集合
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    //3.实时处理数据
    //获取kafkasDtream中的value，也就是一行行的商品
    val goodsLineDsteam: DStream[String] = kafkaDStream.map(_.value())//表示每一条记录，我们就是要从每一条记录中获取这一行商品数据
    val goodsDstream: DStream[String] = goodsLineDsteam.flatMap(_.split(" "))//一个个商品
    val goodsAndOneDstream: DStream[(String, Int)] = goodsDstream.map((_,1))//每个商品记为1
    //val result: DStream[(String, Int)] = goodsAndOneDstream.reduceByKey(_ + _) //无状态聚合

    //定义一个updateFunc:(Seq[V],Option[S]) => Option[Int]，用来将当前批次的数据和历史数据进行聚合
    val updateFunc: (Seq[Int], Option[Int]) => Some[Int] = (currentValues:Seq[Int], historyValue:Option[Int])=>{
      //注意：历史数据第一次为0
      //currentValue.sum当前批次的和
      //historyValue.getOrElse(0)历史数据的值，取不出来说明是第一次，给个默认的0
      val currentResult:Int=currentValues.sum+historyValue.getOrElse(0)
      Some(currentResult)//返回当前批次的和历史结果相加
    }
    val result: DStream[(String, Int)] = goodsAndOneDstream.updateStateByKey(updateFunc)


    //4.输出实时计算结果
    result.print()

    //5.启动并等待程序结果（因为实时程序所以需要一直等待数据到来，也就是需要在这里进行阻塞，不要代码执行到这里就结束）
    ssc.start()
    ssc.awaitTermination()//相当于程序在这里阻塞并一直等待实时数据到来

  }


}
