package com.ohgiraffers.refrigegobackend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
    cacheManager.setCacheNames(java.util.Arrays.asList(
        "users", // 사용자 정보 캐시
        "bookmarks", // 북마크 정보 캐시
        "userIngredients", // 사용자 재료 정보 캐시
        "recipes", // 레시피 정보 캐시
        "ingredients" // 재료 정보 캐시
    ));
    return cacheManager;
  }
}