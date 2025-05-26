package com.ohgiraffers.refrigegobackend.common.util;

import com.ohgiraffers.refrigegobackend.user.dto.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 관련 유틸리티 클래스
 */
public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 ID를 반환
     * 인증되지 않은 경우 null 반환
     * 
     * @return 사용자 ID 또는 null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        
        return null;
    }

    /**
     * 현재 인증된 사용자의 loginId를 반환
     * 인증되지 않은 경우 null 반환
     * 
     * @return loginId 또는 null
     */
    public static String getCurrentLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        
        return null;
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     * 
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getPrincipal().equals("anonymousUser");
    }
}
