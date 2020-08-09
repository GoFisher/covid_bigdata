package cn.zjdf.bean;

public class MaterialBean {
    private String name;//物资名称
    private String from;//物资来源
    private Integer count;//物资数量


    public MaterialBean() {
    }

    public MaterialBean(String name, String from, Integer count) {
        this.name = name;
        this.from = from;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MaterialBean{" +
                "name='" + name + '\'' +
                ", from='" + from + '\'' +
                ", count=" + count +
                '}';
    }
}