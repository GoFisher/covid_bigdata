package cn.zjdf.bean

case class CovidBean(
                      provinceName: String,//省份名称
                      provinceShortName: String,//省份短名
                      cityName: String,//城市名称
                      currentConfirmedCount: Int,//当前确诊
                      confirmedCount: Int,//累加确诊
                      suspectedCount: Int,//疑似病例
                      curedCount: Int,//治愈数
                      deadCount: Int,//死亡数
                      locationId: Int,//位置id
                      pid: Int,//父id
                      cities: String,//下属城市
                      statisticsData: String,//每一天的统计数据
                      datetime: String//日期
                    )