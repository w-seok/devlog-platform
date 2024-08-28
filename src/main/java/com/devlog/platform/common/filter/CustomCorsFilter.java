package com.devlog.platform.common.filter;

import static com.devlog.platform.common.util.RequestUtils.*;
import static java.lang.Boolean.*;
import static org.springframework.data.mapping.Alias.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;

import java.io.IOException;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.devlog.platform.common.config.IpAccessManager;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomCorsFilter implements Filter {

	private final IpAccessManager ipAccessManager;
	private static final List<String> ALLOW_ORIGINS = List.of(
		".wonseoksh.in", "localhost"
	);

	private static final String ALLOWED_METHODS = "GET, POST, PUT, OPTIONS, DELETE";
	private static final String ALLOWED_HEADERS = "X-Requested-With,Origin,Content-Type,Accept,Authorization";

	/**
	 * 허용된 도메인만 열어주기
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		final HttpServletRequest req = (HttpServletRequest)request;
		final HttpServletResponse res = (HttpServletResponse)response;
		final String origin = ((HttpServletRequest)request).getHeader(ORIGIN);

		if (ofNullable(origin).isPresent()) {
			log.debug("헤더확인: {}", getIp(req));

			if (ipAccessManager.contains(req) || isAllowOrigin(origin)) {
				res.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
				res.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, TRUE.toString());

				if (OPTIONS.matches(req.getMethod())) {
					res.addHeader(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
					res.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
					res.setStatus(HttpStatus.OK.value());
					return;
				}
			} else {
				res.setStatus(HttpStatus.FORBIDDEN.value());
				return;
			}
		}
		chain.doFilter(req, res);

	}

	private static boolean isAllowOrigin(String origin) {
		for (String allowOrigin : ALLOW_ORIGINS) {
			if (origin.contains(allowOrigin)) {
				return true;
			}
		}
		return false;
	}
}
