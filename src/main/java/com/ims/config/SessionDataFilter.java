/*package com.ims.config;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ims.bo.UserCredential;
import com.ims.service.SessionDataService;

@Component
public class SessionDataFilter implements Filter {
	@Autowired
	SessionDataService sessionDataService;

	@Value("${filter.url-exclusion-list}")
	private List<String> urlExclusionList;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			if (request != null) {

				String requestUrl = ((HttpServletRequest) request).getRequestURL().toString().toLowerCase();

				AtomicBoolean ignoreUrl = new AtomicBoolean(false);

				urlExclusionList.forEach((url) -> {
					if (requestUrl.trim().contains(url.trim().toLowerCase())) {
						ignoreUrl.set(true);
					}
				});

				if (!ignoreUrl.get()) {

					HttpSession curentSession = ((HttpServletRequest) request).getSession();
					UserCredential userCredential = sessionDataService.getUserCredential(curentSession.getId());

					if (curentSession.getAttribute("AUTH_TOKEN") == null) {
						((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
								"Invalid Session.");
					} else if (!userCredential.getUserToken()
							.equals(((List<String>) curentSession.getAttribute("AUTH_TOKEN")).get(0))) {
						((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
								"Authentication Failed.");
					}
				}

				chain.doFilter(request, response);

			} else {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Request.");
			}
		} catch (Exception ex) {
			throw new ServletException("Unknown exception in RestAuthSecurityFilter", ex);
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig filterconfig) throws ServletException {

	}
}
*/