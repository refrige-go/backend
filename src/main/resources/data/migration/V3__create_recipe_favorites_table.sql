-- 레시피 찜하기 테이블 생성
CREATE TABLE recipe_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    recipe_id VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_recipe_id (recipe_id),
    UNIQUE KEY uk_user_recipe (user_id, recipe_id)
);
