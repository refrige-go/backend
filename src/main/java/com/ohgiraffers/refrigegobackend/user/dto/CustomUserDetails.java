package com.ohgiraffers.refrigegobackend.user.dto;


import com.ohgiraffers.refrigegobackend.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
// 로그인한 유저 정보 표현, 사용자가 입력한 로그인 정보가 db와 일치할때 로그인한 유저에게 발급하는 정보
// Spring Security에서 사용자 정보를 담는 객체
// Security는 CustomUserDetails.getUsername() 등의 형태로, 로그인한 유저의 정보를 사용함
// userdeails의 비밀번호를 로그인 필터에서의 로그인 시점에 사용자가 입력한 로그인 비밀번호와 비교하는데 일치하면
// Authentication 객체가 생성되고, 그안에 UserDetails가 저장됨
public class CustomUserDetails  implements UserDetails {

    // 생성자 주입을 위한 필드
    private final User user;

    public CustomUserDetails(User user) {

        this.user = user;
    }


    // getAuthorities() – 사용자 권한 반환
    // GrantedAuthority는 권한을 표현하는 인터페이스
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return user.getRole();
            }
        });
        // userEntity.getRole()에서 권한 정보를 가져오고, 그걸 하나짜리 리스트(collection)로 반환
        return collection;
    }


    @Override
    public String getPassword() {

        return user.getPassword();
    }


    @Override
    public String getUsername() {

        return user.getUsername();
    }

    // 4가지는 계정의 상태를 나타냄
    // 계정이 만료되지 않았는가?
    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    // 계정이 잠기지 않았는가?
    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    // 비밀번호가 만료되지 않았는가?
    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    // 계정이 활성화 상태인가?
    @Override
    public boolean isEnabled() {

        return true;
    }

    public Long getUserId() {
        return user.getId();
    }
}
