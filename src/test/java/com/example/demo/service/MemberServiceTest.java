package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JoinFormDto;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Member createMember() {

        JoinFormDto memberFormDto = new JoinFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");

        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void JoinTest() {

        // create a new member
        Member member = createMember();
        memberService.saveMember(member);

        // when
        Member savedMember = memberRepository.findByEmail(member.getEmail());

        // then
        assertEquals(savedMember.getEmail(), member.getEmail());
        assertEquals(savedMember.getName(), member.getName());

    }

    @Test
    @DisplayName("중복 회원 테스트")
    public void duplicateCheckTest() {

        // create a new member
        Member m1 = createMember();
        Member m2 = createMember();

        memberService.saveMember(m1);

        // when
        Throwable e = assertThrows(IllegalStateException.class, () -> {
            memberService.saveMember(m2);
        });

        // then
        assertEquals("이미 가입된 회원입니다.", e.getMessage());

    }
}
