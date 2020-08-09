package cn.zjdf.scala_study

/**
 * @Classname SyntaxStudy2
 * @Date 2020-08-04 22:58
 * @Created by Yujz
 * @Description TODO
 */
object SyntaxStudy2 {
  def main(args: Array[String]): Unit = {
    println(sum(1,100))
  }

  //定义格式
  //def 方法（参数名1：参数类型1，参数名2：参数类型...）：返回值=｛方法体｝
  //注意：返回值类型可以省略（递归调用除外），｛｝中如果只有一句表达式，那么｛｝也可以省略
  //演示：定义一个方法完成从n加到m，如1-100，就是从1+2+3+..100

  def sum(n:Int,m:Int):Int ={
    var result = 0
    for (i<- n to m){
      result+=i
    }
    result
  }
}
