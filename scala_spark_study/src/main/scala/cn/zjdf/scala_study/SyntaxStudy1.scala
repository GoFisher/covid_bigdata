package cn.zjdf.scala_study

/**
 * @Classname SyntaxStudy1
 * @Date 2020-08-04 22:20
 * @Created by Yujz
 * @Description TODO
 */
object SyntaxStudy1 {
  def main(args: Array[String]): Unit = {
    //定义变量
    //语法格式：
    //语法1 val 变量名：类型=值 //：类型可以省略
    val name = "zjdf"

    //name = "jl" //会报错，因为var修饰的变量不能被重新赋值，类似于java中的final修饰的

    //语法2： var 变量名：类型 = 值 //：类型可以类型
    var age = 18
    age = 20 // 正确，因为var修饰的变量能够被重新赋值，类似于java中的普通变量

    //val 和 var什么时候用？
    // 优先选用val，如果不满足再使用var
    //因为scala多用在大数据分布式环境，val修饰变量后，变量值不能被修改，可以避免很多并发问题

    //2.条件表达式
    if (age > 18) {
      println("已成年")
    } else {
      println("未成年")
    }
    //注意:{}表达式的最后一行，可以作为返回值

    val result = if (age > 18) {
      "xx"
      "已成年" //最后一行，可以作为返回值
    } else {
      "未成年" //最后一行，可以作为返回值
    }
    println(result) //已成年

    //3.循环表达式
    for (i <- List(1, 2, 3, 4, 5)) {
      println(i)
    }

    var j = 6
    while (j <= 10) {
      println(j)
      j += 1 // j++不支持
    }

  }

}
