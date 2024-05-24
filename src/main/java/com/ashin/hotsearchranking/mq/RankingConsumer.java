package com.ashin.hotsearchranking.mq;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.ashin.hotsearchranking.constant.RankingConstant;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RankingConsumer {

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	private JavaSparkContext sparkContext;
	private ArrayList<String> keywords;

	@PostConstruct
	public void init(){
		sparkContext = new JavaSparkContext(new SparkConf()
				.setMaster("local")
				.setAppName("RankingCounter"));
		keywords = new ArrayList<>();
		redisTemplate.delete(RankingConstant.REDIS_RANKING_KEY);
	}

	@RabbitListener(queues = RankingConstant.MQ_RANKING_QUEUE)
	public void process(String keyword) {

		keywords.add(keyword);
		// 使用Spark进行计算

		Map<String, Long> counts = sparkContext.parallelize(keywords).countByValue();

		long count = counts.get(keyword);

		// 将结果存入Redis
		redisTemplate.opsForZSet().add(RankingConstant.REDIS_RANKING_KEY, keyword, count);
	}
}