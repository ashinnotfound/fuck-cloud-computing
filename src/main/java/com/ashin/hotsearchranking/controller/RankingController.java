package com.ashin.hotsearchranking.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import com.ashin.hotsearchranking.constant.RankingConstant;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@CrossOrigin
public class RankingController {
	@Resource
	private RabbitTemplate rabbitTemplate;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@PostMapping
	public ResponseEntity<String> add(@RequestBody String keyword) {
		rabbitTemplate.convertAndSend(RankingConstant.MQ_RANKING_QUEUE, keyword);
		return ResponseEntity.ok("搜索成功");
	}

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> get(@RequestParam(defaultValue = "10") int limit) {
		Set<ZSetOperations.TypedTuple<Object>> results = redisTemplate.opsForZSet().reverseRangeWithScores(RankingConstant.REDIS_RANKING_KEY, 0, limit - 1);
		List<Map<String, Object>> response = new ArrayList<>();
		if (results != null){
			for (ZSetOperations.TypedTuple<Object> result : results) {
				Map<String, Object> item = new HashMap<>();
				item.put("keyword", result.getValue());
				item.put("count", Objects.requireNonNull(result.getScore()).intValue());
				response.add(item);
			}
		}

		return ResponseEntity.ok(response);
	}
}
