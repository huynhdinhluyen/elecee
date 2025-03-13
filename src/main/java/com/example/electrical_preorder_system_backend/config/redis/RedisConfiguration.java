//package com.example.electrical_preorder_system_backend.config.redis;
//
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//
//@Configuration
//@EnableCaching
//public class RedisConfiguration {
//
//    @Bean(name = "redisObjectMapper")
//    public ObjectMapper redisObjectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance,
//                ObjectMapper.DefaultTyping.EVERYTHING,
//                JsonTypeInfo.As.PROPERTY);
//        return mapper;
//    }
//
//    @Bean
//    public GenericJackson2JsonRedisSerializer redisSerializer(
//            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
//        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(
//            RedisConnectionFactory factory,
//            GenericJackson2JsonRedisSerializer redisSerializer) {
//
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(redisSerializer);
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(redisSerializer);
//        template.afterPropertiesSet();
//
//        return template;
//    }
//
//    @Bean
//    public CacheManager cacheManager(
//            RedisConnectionFactory factory,
//            GenericJackson2JsonRedisSerializer redisSerializer) {
//
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(60))
//                .disableCachingNullValues()
//                .serializeKeysWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
//
//        return RedisCacheManager.builder(factory)
//                .cacheDefaults(config)
//                .build();
//    }
//
//}