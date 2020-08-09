package cn.zjdf.scala_study

/**
 * @Classname SyntaxStudy3
 * @Date 2020-08-04 23:07
 * @Created by Yujz
 * @Description TODO
 */
object SyntaxStudy3 {
  def main(args: Array[String]): Unit = {
    //函数的格式
    //语法1：完整语法
    //val 函数名：(参数类型)=>返回值类型=(参数名:参数类型)=>{函数体}
    val add1:(Int,Int)=>Int=(a:Int,b:Int)=>{a+b}

    //语法2：简写语法（更常用）
    //val 函数名=(参数名：参数类型)=>{函数体}
    val add2=(a:Int,b:Int)=>{a+b}
    println(add1(1,2))
    println(add2(1,2))
  }
}
