package com.kyunghwan.loginskeleton.Account;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyunghwan.loginskeleton.Account.dto.AccountDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @DisplayName("로그인 화면 조회 테스트")
    @Test
    public void getSignIn() throws Exception {
        mockMvc.perform(get("/sign-in"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-in"))
        ;
    }

    @DisplayName("회원가입 화면 조회 테스트")
    @Test
    public void getSignUp() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
        ;
    }

    @DisplayName("회원가입 성공 테스트")
    @Test
    public void successSignUp() throws Exception {
        String email = "email@email.com";
        String password = "password";

        AccountDTO accountDTO = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        ResultActions resultActions = mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
        ;

        String string = resultActions.andReturn().getResponse().getContentAsString();
        String msg = "회원가입 성공!";
        assertThat(string).contains(msg);

        Account account = accountRepository.findByEmail(email);
        assertThat(email).isEqualTo(account.getEmail());
        assertThat(password).isNotEqualTo(account.getPassword());
    }

    @DisplayName("회원가입 실패 테스트")
    @ParameterizedTest(name = "#{index} : {2}")
    @MethodSource("params")
    public void failureSignUp(String email, String password, String message) throws Exception {

        AccountDTO accountDTO = AccountDTO.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    private static Stream<Arguments> params() {
        return Stream.of(
                Arguments.of(null, "password", "이메일 입력 X"),
                Arguments.of("email@email.com", null, "패스워드 입력 X"),
                Arguments.of(null, null, "이메일 입력 X, 패스워드 입력 X"),
                Arguments.of("email", "password", "이메일 형식 오류"),
                Arguments.of("email@email.com", "11", "패스워드 8자 미만"),
                Arguments.of("email@email.com", "111111111111111111111111111", "패스워드 20자 초과"),
                Arguments.of("email", "11", "이메일, 패스워드 형식 오류")
        );
    }

}