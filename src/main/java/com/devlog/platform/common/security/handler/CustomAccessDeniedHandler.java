package com.devlog.platform.common.security.handler;

import static com.devlog.platform.common.util.RequestUtils.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAccessDeniedHandler extends AccessDeniedHandlerImpl {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		super.handle(request, response, accessDeniedException);

		if (log.isDebugEnabled()) {
			log.debug("[{}] [{}] [{}] [{}]", getIp(request), HttpStatus.FORBIDDEN, request.getMethod(),
				request.getRequestURI());
		}
	}
}

