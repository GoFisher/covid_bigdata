package cn.zjdf.scala_study

import scala.collection.immutable

/**
 * @Classname SyntaxStudy3
 * @Date 2020-08-04 23:07
 * @Created by Yujz
 * @Description Scala-函数式Api学习
 */
object SyntaxStudy4 {
  def main(args: Array[String]): Unit = {
    //函数的格式
    //语法1：完整语法
    //val 函数名：(参数类型)=>返回值类型=(参数名:参数类型)=>{函数体}
    //    val add1:(Int,Int)=>Int=(a:Int,b:Int)=>{a+b}

    //语法2：简写语法（更常用）
    //val 函数名=(参数名：参数类型)=>{函数体}
    //    val add2=(a:Int,b:Int)=>{a+b}
    //    println(add1(1,2))
    //    println(add2(1,2))

    //Scala的函数式API主要是提供给集合（Array,list,Set,Map...）使用，spark/flink中的函数是API也
    //是给spark/flink的分布式集合使用

    //0.准备一个集合
    val list: immutable.Seq[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    println("======普通遍历======")
    for (i <- list) {
      println(i)
    }

    println("======函数式遍历======")
    val fun = (i: Int) => {
      println(i)
    }
    list.foreach(fun)
    //list.foreach(i=>println(i))
    //list.foreach(println(_))_表示list中的每一个元素
    //list.foreach(println) 行为参数化

    println("===普通偶数过滤====")
    for (i <- list) {
      if (i % 2 == 0) {
        println(i)
      }
    }

    println("===函数式偶数过滤===")
    val os = (i: Int) => (
      i % 2 == 0
      )
    list.filter(os).foreach(println)
    //list.filter(_%2==0).foreach(println)

    println("===排序===")
    val nums: List[Int] = List(1,4,3,8,10,5,9)
    nums.sortBy(i=>i).foreach(println) //默认是升序排序，i=>-i 取相反数后再进行排序，就是倒叙排序

    println("===映射===")
    nums.map(10*_).foreach(println)

    println("===扁平化映射===")
    val clastu = List("tom jack","vicky lucy") //表示学生组数

    //将class_stu中的学生变成一个集合["tom","jack","vicky","lucy"]
    //方式1：
    val stringses:List[Array[String]] = clastu.map(_.split(" "))
    val result = stringses.flatten
    println(result)

    //方式2：
    println(clastu.flatMap(_.split(" ")))

    println("===分组===")
    val students=List(("张三","男"),("李四","男"),("王武","女"),("赵六","女"))
    //按照学生性别进行分组
    println(students.groupBy(_._2)) //第1个_表示students中的每一个学生，第二个_2按照学生的性别即第2个参数

    println("===归约===")
    //求list中的累加和
    println("最大最小和："+(nums.min+nums.max))

  }
}
