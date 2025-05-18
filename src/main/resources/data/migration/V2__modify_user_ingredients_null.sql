-- user_ingredients 테이블에서 expiry_date 컬럼의 타입을 DATE로 변경하고, NULL 허용으로 수정
ALTER TABLE user_ingredients
    MODIFY COLUMN expiry_date DATE NULL;

-- user_ingredients 테이블에서 purchase_date 컬럼의 타입을 DATE로 변경하고, NULL 허용으로 수정
ALTER TABLE user_ingredients
    MODIFY COLUMN purchase_date DATE NULL;

-- recipes 테이블에서 rcp_parts_dtls 컬럼 타입을 TEXT로 변경 (길이 제한 해제)
ALTER TABLE recipes MODIFY rcp_parts_dtls TEXT;

-- recipes 테이블에서 manual01 컬럼 타입을 TEXT로 변경 (길이 제한 해제)
ALTER TABLE recipes MODIFY manual01 TEXT;

-- recipes 테이블에서 manual02 컬럼 타입을 TEXT로 변경 (길이 제한 해제)
ALTER TABLE recipes MODIFY manual02 TEXT;
