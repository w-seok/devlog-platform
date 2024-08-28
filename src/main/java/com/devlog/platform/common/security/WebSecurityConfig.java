package com.devlog.platform.common.security;

import static com.devlog.platform.common.enums.AccountRole.*;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.devlog.platform.common.config.IpAccessManager;
import com.devlog.platform.common.security.handler.CustomAccessDeniedHandler;
import com.devlog.platform.common.security.handler.CustomAuthenticationEntryPointHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
	private final CustomAuthorizationFilter customAuthorizationFilter;
	private final IpAccessManager ipAccessManager;

	@Profile("!test")
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding(StandardCharsets.UTF_8.name());
		filter.setForceEncoding(true);
		http
			.addFilterBefore(filter, CsrfFilter.class)
			.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

		// 로그인 방식 설정
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// 경로별 접근 제어 설정
		http
			.authorizeHttpRequests((authorizeRequests) ->
				authorizeRequests
					.requestMatchers("/actuator/**", "/error-test")
					.access((authentication, object) -> {
						// 관리자 혹은 허용된 ip의 경우만 접근 가능
						var isAdmin = authentication.get()
							.getAuthorities()
							.stream()
							.anyMatch(it -> it.getAuthority().equals(ADMIN.getValue()));
						return new AuthorizationDecision(
							isAdmin || ipAccessManager.isMetricNetwork(object.getRequest()));
					})

					// 기본 보안망(내부 네트워크 및 허용된 ip) 에서만 회원가입 및 특정 경로 접근 허용
					.requestMatchers("/api/account/sign-up", "/docs/**", "/example/**")
					.access((authentication, object) -> new AuthorizationDecision(
						ipAccessManager.isPrivateNetwork(authentication.get(), object.getRequest())))

					// 로그인 관련 경로 및 특정 uri의 경우 접근 허용
					.requestMatchers("/api/account/sign-in", "/test", "/api/constants/**")
					.permitAll()

					// health check
					.requestMatchers("/api/health")
					.authenticated()

					// swagger
					.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**")
					.permitAll()

					// local api 경로
					.requestMatchers("/api/local", "/api/local/**")
					.access((authentication, object) -> {
						String ip = object.getRequest().getRemoteAddr();
						boolean isLocalIp = "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
						return new AuthorizationDecision(
							isLocalIp || ipAccessManager.isPrivateNetwork(authentication.get(), object.getRequest()));
					})

					// 나머지 경로는 인증된 사용자만 접근 가능
					.anyRequest()
					.authenticated());

		// 예외처리 커스터마이징
		http
			.exceptionHandling((exceptionConfig) ->
				exceptionConfig.authenticationEntryPoint(customAuthenticationEntryPointHandler)
					.accessDeniedHandler(accessDeniedHandler()));

		return http.build();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
