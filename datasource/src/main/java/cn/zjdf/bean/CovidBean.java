package cn.zjdf.bean;

public class CovidBean {
    private String provinceName;//省份名称
    private String provinceShortName;//省份短名
    private String cityName;//城市名称
    private Integer currentConfirmedCount;//当前确诊数量
    private Integer confirmedCount;//累计确诊数量
    private Integer suspectedCount;//疑似病例数量
    private Integer curedCount;//治愈数量
    private Integer deadCount;//死亡数量
    private Integer locationId;//位置id
    private Integer pid;//父级id
    private String cities;//下属城市
    private String statisticsData;//每一天的统计数据
    private String datetime;//时间

    public CovidBean(String provinceName, String provinceShortName, String cityName, Integer currentConfirmedCount, Integer confirmedCount, Integer suspectedCount, Integer curedCount, Integer deadCount, Integer locationId, Integer pid, String cities, String statisticsData, String datetime) {
        this.provinceName = provinceName;
        this.provinceShortName = provinceShortName;
        this.cityName = cityName;
        this.currentConfirmedCount = currentConfirmedCount;
        this.confirmedCount = confirmedCount;
        this.suspectedCount = suspectedCount;
        this.curedCount = curedCount;
        this.deadCount = deadCount;
        this.locationId = locationId;
        this.pid = pid;
        this.cities = cities;
        this.statisticsData = statisticsData;
        this.datetime = datetime;
    }

    public CovidBean() {
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceShortName() {
        return provinceShortName;
    }

    public void setProvinceShortName(String provinceShortName) {
        this.provinceShortName = provinceShortName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getCurrentConfirmedCount() {
        return currentConfirmedCount;
    }

    public void setCurrentConfirmedCount(Integer currentConfirmedCount) {
        this.currentConfirmedCount = currentConfirmedCount;
    }

    public Integer getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(Integer confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public Integer getSuspectedCount() {
        return suspectedCount;
    }

    public void setSuspectedCount(Integer suspectedCount) {
        this.suspectedCount = suspectedCount;
    }

    public Integer getCuredCount() {
        return curedCount;
    }

    public void setCuredCount(Integer curedCount) {
        this.curedCount = curedCount;
    }

    public Integer getDeadCount() {
        return deadCount;
    }

    public void setDeadCount(Integer deadCount) {
        this.deadCount = deadCount;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getCities() {
        return cities;
    }

    public void setCities(String cities) {
        this.cities = cities;
    }

    public String getStatisticsData() {
        return statisticsData;
    }

    public void setStatisticsData(String statisticsData) {
        this.statisticsData = statisticsData;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "CovidBean{" +
                "provinceName='" + provinceName + '\'' +
                ", provinceShortName='" + provinceShortName + '\'' +
                ", cityName='" + cityName + '\'' +
                ", currentConfirmedCount=" + currentConfirmedCount +
                ", confirmedCount=" + confirmedCount +
                ", suspectedCount=" + suspectedCount +
                ", curedCount=" + curedCount +
                ", deadCount=" + deadCount +
                ", locationId=" + locationId +
                ", pid=" + pid +
                ", cities='" + cities + '\'' +
                ", statisticsData='" + statisticsData + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }
}