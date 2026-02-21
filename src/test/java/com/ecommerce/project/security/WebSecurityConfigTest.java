package com.ecommerce.project.security;

import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = WebSecurityConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class WebSecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void authEntryPointBeanExists() {
        assertThat(context.getBean(AuthEntryPointJwt.class)).isNotNull();
    }

    @Test
    void authTokenFilterBeanExists() {
        assertThat(context.getBean(AuthTokenFilter.class)).isNotNull();
    }

    @Test
    void authenticationManagerBeanExists() {
        assertThat(context.getBean(AuthenticationManager.class)).isNotNull();
    }
}