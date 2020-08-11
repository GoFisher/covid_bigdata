package cn.zjdf.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Mapper //表示interface是MyBatis的Mapper,在运行是会生成代理对象查询数据库
@Component//表示交给SpringBoot管理
public interface CovidMapper {

//    CREATE TABLE `covid19_1` (
//            `datetime` varchar(20) NOT NULL DEFAULT '',
//            `currentConfirmedCount` bigint DEFAULT '0',
//            `confirmedCount` bigint DEFAULT '0',
//            `suspectedCount` bigint DEFAULT '0',
//            `curedCount` bigint DEFAULT '0',
//            `deadCount` bigint DEFAULT '0',
//    PRIMARY KEY (`datetime`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8;


    @Select("select datetime,currentConfirmedCount,confirmedCount,suspectedCount,curedCount,deadCount from covid19_1 where datetime = #{datetime}")
    List<Map<String, Object>> getNationalData(String datetime);

    //注意:前端需要的是省份/城市名称和累加确诊数量,而且前端的key名称已经定好了,叫name和value
    @Select("select provinceShortName as name,confirmedCount as value from covid19_2 where datetime = #{datetime}")
    List<Map<String, Object>> getNationalMapData(String datetime);

    @Select("select dateId,confirmedIncr as `新增确诊`,confirmedCount as `累计确诊`,suspectedCount as `疑似病例`,curedCount as `累计治愈`,deadCount as `累计死亡` from covid19_3")
    List<Map<String, Object>> getCovidTimeData();

    //注意:前端只需要省份名称和境外输入确诊数量,而且前端的key名称已经定好了,叫name和value
    @Select("select provinceShortName as name,confirmedCount as value from covid19_4 where datetime = #{datetime} order by value desc limit 10")
    List<Map<String, Object>> getCovidImportData(String datetime);

    @Select("select name,cg as `采购`,xb as `下拨`,jz as `捐赠`,xh as `消耗`,xq as `需求`,kc as `库存` from covid19_wz")
    List<Map<String, Object>> getCovidWz();
}

