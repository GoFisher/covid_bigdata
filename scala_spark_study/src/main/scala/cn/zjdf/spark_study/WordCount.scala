package cn.zjdf.spark_study

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @Classname WordCount
 * @Date 2020-08-05 7:36
 * @Created by Yujz
 * @Description 使用spark完成，商品数量统计
 */
object WordCount {
  def main(args: Array[String]): Unit = {
    //1.准备spark开发环境,local[*]表示用所有资源（cpu）来模拟spark集群环境
    //（用本地的多线程来模拟spark集群中的master，slave）
    //实际开发中也是这样，在本地开发测试，然后打包放到linux集群去运行
    //（需要使用spark-submit去提交使用）
    val conf: SparkConf = new SparkConf().setAppName("WordCount").setMaster("local[*]")
    val context: SparkContext = new SparkContext(conf)
    context.setLogLevel("WARN")

    //2.加载数据
    val fileRdd: RDD[String] = context.textFile("scala_spark_study/goods.log")

    //3.处理数据
    //每一行数据进行切割，并进行压扁
    val WordMap: RDD[(String, Int)] = fileRdd.flatMap(_.split(" ")).map((_, 1))
    val goodsRdd: RDD[(String, Int)] = WordMap.reduceByKey(_ + _)


    //4.输出结果
    val result: Array[(String, Int)] = goodsRdd.collect()
    result.sortBy(-_._2).foreach(println)

  }

}
