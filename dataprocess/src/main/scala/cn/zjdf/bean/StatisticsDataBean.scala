package cn.zjdf.bean

case class StatisticsDataBean(
                               var dateId: String,
                               var provinceShortName: String,
                               var locationId:Int,
                               var confirmedCount: Int,
                               var currentConfirmedCount: Int,
                               var confirmedIncr: Int,
                               var curedCount: Int,
                               var currentConfirmedIncr: Int,
                               var curedIncr: Int,
                               var suspectedCount: Int,
                               var suspectedCountIncr: Int,
                               var deadCount: Int,
                               var deadIncr: Int
                               )