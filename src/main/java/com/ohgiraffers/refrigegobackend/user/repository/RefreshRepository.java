package com.ohgiraffers.refrigegobackend.user.repository;

import com.ohgiraffers.refrigegobackend.user.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends CrudRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

}
