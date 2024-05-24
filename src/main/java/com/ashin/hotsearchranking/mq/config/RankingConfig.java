package com.ashin.hotsearchranking.mq.config;

import com.ashin.hotsearchranking.constant.RankingConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RankingConfig {
	@Bean
	public Queue rankKeywordQueue() {
		return new Queue(RankingConstant.MQ_RANKING_QUEUE, true);
	}
}