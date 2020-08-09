package cn.zjdf.util

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.sql.{ForeachWriter, Row}
abstract class BaseJdbcSink(sql:String) extends ForeachWriter[Row] {
  var conn: Connection = _
  var ps: PreparedStatement = _
  override def open(partitionId: Long, version: Long): Boolean = {
    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/covid19_bigdata?characterEncoding=UTF-8&useSSL=true&serverTimezone=GMT%2B8","root","root")
    true
  }

  override def process(value: Row): Unit = {
    realProcess(sql,value)
  }

  def realProcess(sql:String,value: Row)

  override def close(errorOrNull: Throwable): Unit = {
    if (conn != null) {
      conn.close
    }
    if (ps != null) {
      ps.close()
    }
  }
}