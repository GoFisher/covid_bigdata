package cn.zjdf.process

import java.sql.{Connection, DriverManager, PreparedStatement}

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Author Yujz
 * Date 2020年8月5日 14:19:51
 * Desc 物资数据的处理分析
 * 需求:
 * (name=医用外科口罩/个, from=需求, count=752)
 * (name=防护服/套, from=消耗, count=126)
 * (name=84消毒液/瓶, from=捐赠, count=515)
 * (name=84消毒液/瓶, from=需求, count=448)
 * 对数据生成模块发送到Kafka的数据进行统计分析,得出各个物资的基本情况,如下格式:
 * 名称  					采购 		下拨  	捐赠    消耗   需求   	库存
 * 医用外科口罩/个  1000		500		  500  	  500	   500	  1000
 *
 * .....
 */
object Covid19WZDataProcess {
  def main(args: Array[String]): Unit = {
    System.setProperty("hadoop.home.dir", "D:\\Software\\hadoop-2.6.4") //设置winutils位置

    //1.准备SparkStreaming执行环境
    val conf: SparkConf = new SparkConf().setAppName("Covid19WZDataProcess").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc: StreamingContext = new StreamingContext(sc, Seconds(5))
    ssc.checkpoint("./ckpdir") //该目录会自动创建


    //2.连接Kafka实时获取数据
    //2.1准备Kafka连接参数
    val kafkaParams: Map[String, Object] = Map[String, Object](
      //kafka集群地址,如果连接的是windows的,写localhost:9092即可
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer], //key的反序列化类型
      "value.deserializer" -> classOf[StringDeserializer], //value的反序列化类型
      "group.id" -> "SparkKafka", //消费者组名称,随便指定一个名称即可
      "auto.offset.reset" -> "latest", //表示如果有offset记录就从offset记录开始消费,如果没有就从最新的数据开始消费,offset是用来记录消费到哪一条数据了
      "enable.auto.commit" -> (true: java.lang.Boolean) //使用自动提交offset
    )
    val topics = Array("covid19_wz") //表示要订阅哪个主题covid19_wz


    //2.2真正的连接Kafka获取数据
    //kafkaDStream: InputDStream[ConsumerRecord[String, String]]
    //kafkaDStream就是从Kafka中实时消费到的数据,DStream可以理解为时间上连续的RDD组成的实时的分布式集合
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    //3.数据格式处理
    val wzDataJsonDS: DStream[String] = kafkaDStream.map(_.value())
    //    wzDataJsonDS.print()

    //通过观察收到的数据为上面的json格式,首先可以解析为一个jsonObject,然后再取出里面的name,from,count
    //然后将每一条数据转为下面的格式:
    //名称  采购 	下拨  捐赠   消耗  需求   库存
    val tupleDS: DStream[(String, (Int, Int, Int, Int, Int, Int))] = wzDataJsonDS.map(jsonStr => {
      //json格式Str解析为一个jsonObject,然后再取出里面的name,from,count
      val jsonObject: JSONObject = JSON.parseObject(jsonStr)
      val name: String = jsonObject.getString("name")
      val from: String = jsonObject.getString("from")
      val count: Int = jsonObject.getInteger("count")
      //接下来每一条数变为下面的格式:
      //名称  采购 	下拨  捐赠   消耗  需求   库存
      from match {
        case "采购" => (name, (count, 0, 0, 0, 0, count))
        case "下拨" => (name, (0, count, 0, 0, 0, count))
        case "捐赠" => (name, (0, 0, count, 0, 0, count))
        case "消耗" => (name, (0, 0, 0, -count, 0, -count))
        case "需求" => (name, (0, 0, 0, 0, -count, -count))
        //消耗和需求都需要减库存,所以为-负的
        //名称  	采购 		下拨  	捐赠    消耗   需求   	库存
        //口罩/个  1000		500		  500  	  -500	-500	  1000
      }
      //上面的这一串模式匹配语法其实相当于if-else或java中的switch-case
    })


    //4.数据统计
    //对上面的key相同的数据(物资名称相同的数据要进行聚合),如:
    //名称  	采购 		下拨  	捐赠    消耗   需求   	库存
    //(84消毒液/瓶,(0,0,100,0,0,100))
    //(84消毒液/瓶,(0,0,0,-50,0,-50))
    //那么目前84消毒液的实时物资情况为:
    //(84消毒液/瓶,(0,0,100,-50,0,50))
    //其实就是相应位置相加就得到结果
    //过程有点绕--但是其实就是生活中的记账,有加有减,但是数据中在前面已经将需要减去的用-表示了,所以对每条数据直接相加即可
    //也就是说接下来只需要对数据按照key进行聚合即可(注意:需要有状态)
    //currentValues:Seq[(Int, Int, Int, Int, Int, Int)]：表示当前key对应多条数据，如(0,0,100,0,0,100)
    //historyValue:Option[(Int, Int, Int, Int, Int, Int)])：表示历史值，注意初始化((0,0,0,0,0,0))
    val updateFunc = (currentValues: Seq[(Int, Int, Int, Int, Int, Int)], historyValue: Option[(Int, Int, Int, Int, Int, Int)]) => {
      var current_cg: Int = 0
      var current_xb: Int = 0
      var current_jz: Int = 0
      var current_xh: Int = 0
      var current_xq: Int = 0
      var current_kc: Int = 0
      if (currentValues.size > 0) {
        //循环当前批次的数据
        for (i <- 0 until currentValues.size) {
          current_cg += currentValues(i)._1
          current_xb += currentValues(i)._2
          current_jz += currentValues(i)._3
          current_xh += currentValues(i)._4
          current_xq += currentValues(i)._5
          current_kc += currentValues(i)._6
        }
        //获取以前批次值
        val history_cg: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._1
        val history_xb: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._2
        val history_jz: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._3
        val history_xh: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._4
        val history_xq: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._5
        val history_kc: Int = historyValue.getOrElse((0, 0, 0, 0, 0, 0))._6

        Option((
          current_cg + history_cg,
          current_xb + history_xb,
          current_jz + history_jz,
          current_xh + history_xh,
          current_xq + history_xq,
          current_kc + history_kc
        ))
      } else {
        historyValue //如果当前批次没有数据直接返回之前的值即可
      }
    }

    val resultDS: DStream[(String, (Int, Int, Int, Int, Int, Int))] = tupleDS.updateStateByKey(updateFunc)
    //    resultDS.print()

    //5.数据输出--先输出到控制台,再输出到MySQL
    //5.数据输出--先输出到控制台,再输出到MySQL
    resultDS.print()
    /*
        (防护目镜/副,(913,0,0,-551,-152,210))
        (电子体温计/个,(873,752,64,-821,-448,420))
        (84消毒液/瓶,(0,0,0,-572,0,-572))
        (N95口罩/个,(0,834,145,-1046,0,-67))
        (医用防护服/套,(1469,0,914,0,-322,2061))
        (一次性橡胶手套/副,(190,0,622,0,-60,752))
     */

    //遍历resultDS中的每一条数据并把该数据存入MySQL
    resultDS.foreachRDD(rdd => {
      rdd.foreachPartition(lines => {
        //开启关闭连接的代码在foreachPartition里面,表示每次都开启关闭连接,而这也是需要的
        //因为各个分区其实一般都是各个机器上
        //使用jdbc代码操作数据库即可
        //-1开启数据库连接
        val conn: Connection = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/covid19_bigdata?characterEncoding=UTF-8&useSSL=true&serverTimezone=GMT%2B8", //指定库和编码
          "root", //帐号
          "root" //密码需要改成自己的
        )
        //-2准备SQL
        //replace into表示如果该key存在则更新,不存在则插入
        val sql: String = "replace into covid19_wz(name,cg,xb,jz,xh,xq,kc) values(?,?,?,?,?,?,?)"
        //-3.获取预编译语句对象
        val ps: PreparedStatement = conn.prepareStatement(sql)
        for (line <- lines) {
          val name: String = line._1
          val cg: Int = line._2._1
          val xb: Int = line._2._2
          val jz: Int = line._2._3
          val xh: Int = line._2._4
          val xq: Int = line._2._5
          val kc: Int = line._2._6
          ps.setString(1, name)
          ps.setInt(2, cg)
          ps.setInt(3, xb)
          ps.setInt(4, jz)
          ps.setInt(5, xh)
          ps.setInt(6, xq)
          ps.setInt(7, kc)
          //-4.执行sql
          ps.executeUpdate()
          println("该条数据已更新到数据库")
        }

        //关闭连接
        ps.close()
        conn.close()
      })
    })


    //6.启动并等待结束
    ssc.start()
    ssc.awaitTermination()

  }
}
