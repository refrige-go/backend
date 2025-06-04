package com.ohgiraffers.refrigegobackend.ocr.infrastructure;

import com.ohgiraffers.refrigegobackend.ocr.domain.OcrIngredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrIngredientRepository extends JpaRepository<OcrIngredients, Long> {
    List<OcrIngredients> findAll();
}
