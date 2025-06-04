package com.ohgiraffers.refrigegobackend.ingredient.infrastructure.scheduler;

import com.ohgiraffers.refrigegobackend.ingredient.service.UserIngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IngredientExpirationScheduler {

    private final UserIngredientService userIngredientService;

    @Autowired
    public IngredientExpirationScheduler(UserIngredientService userIngredientService) {
        this.userIngredientService = userIngredientService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleExpiringCheck() {
        userIngredientService.notifyUserAboutExpiringIngredient();
    }
}
