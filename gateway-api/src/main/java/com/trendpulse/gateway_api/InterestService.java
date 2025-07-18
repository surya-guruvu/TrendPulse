package com.trendpulse.gateway_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.UserInterest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class InterestService {
    
    @Autowired
    private KafkaTemplate<String, UserInterest> kafkaTemplate;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private final RedisAvroService redisAvroService;

    InterestService(RedisAvroService redisAvroService) {
        this.redisAvroService = redisAvroService;
    }

    public static List<CharSequence> toCharSequenceList(List<String> strings) {
        return new ArrayList<>(strings);
    }

    public void follow(String userId, List<String> tags){
        System.out.println("YES");
        String redisKey = "interest:" + userId;

        UserInterest current = redisAvroService.getAvro(redisKey, UserInterest.class);

        Set<String> combined = new HashSet<>();


        if (current != null && current.getHashtags() != null) {
            current.getHashtags().forEach(t -> combined.add(t.toString()));
        }

        tags.stream().map(String::toLowerCase)
                    .map(String::strip)
                    .forEach(combined::add);
        
        // Step 2: Build updated interest
        UserInterest updated = UserInterest.newBuilder()
                                        .setUserId(userId)
                                        .setHashtags(toCharSequenceList(new ArrayList<String>(combined)))
                                        .build();

        // redisTemplate.opsForList().leftPush(redisKey,updated);
        redisAvroService.setAvro(redisKey, updated, UserInterest.class);

        kafkaTemplate.send("user.interest",userId,updated);
    }

    public void unfollow(String userId, List<String> tagsToRemove){
        String redisKey = "interest:" + userId;

        UserInterest current = redisAvroService.getAvro(redisKey, UserInterest.class);

        if(current == null){
            return;
        }

        List<String> currentHashTags = current.getHashtags().stream().map(CharSequence::toString)
                                                .filter(tag -> !tagsToRemove.contains(tag))
                                                .toList();

        if(currentHashTags.isEmpty()){
            // 👇 Produce a tombstone → deletes entry from KTable
            kafkaTemplate.send("user.interest", userId, null);
            redisTemplate.delete("interest:" + userId);
        }
        else{
            // Step 2: Build updated interest
            UserInterest updated = UserInterest.newBuilder()
                                            .setUserId(userId)
                                            .setHashtags(toCharSequenceList(currentHashTags))
                                            .build();

            redisAvroService.setAvro(redisKey, updated, UserInterest.class);
            kafkaTemplate.send("user.interest",userId,updated);
        }

    }
}
