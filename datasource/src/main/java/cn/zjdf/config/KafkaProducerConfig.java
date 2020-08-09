package cn.zjdf.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration // 表示该类是一个配置类
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap.servers}")
    private String bootstrap_servers; //localhost:9092

    @Bean //表示方法返回值对象是受Spring所管理的一个Bean
    public KafkaTemplate kafkaTemplate() {
        // 构建工厂需要的配置
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 创建生产者工厂
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory(configs);
        // 返回KafkTemplate的对象
        KafkaTemplate kafkaTemplate = new KafkaTemplate(producerFactory);
        //System.out.println("kafkaTemplate"+kafkaTemplate);
        return kafkaTemplate;
    }
}