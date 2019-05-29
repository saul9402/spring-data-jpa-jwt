package com.bolsadeideas.springboot.datajpa.app.auth.handler;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private MessageSource messagesSource;

	@Autowired
	private LocaleResolver localeResolver;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		SessionFlashMapManager flashMapManager = new SessionFlashMapManager();

		FlashMap flashMap = new FlashMap();

		/*
		 * Como no se puede inyectar el Locale directamente se pide aparti del request
		 */
		Locale locale = localeResolver.resolveLocale(request);
		String mensaje = String.format(messagesSource.getMessage("text.login.success", null, locale),
				authentication.getName());

		flashMap.put("success", mensaje);

		flashMapManager.saveOutputFlashMap(flashMap, request, response);
		if (authentication != null) {
			log.info("el usuario '{}' ha iniciado sesion con éxito", authentication.getName());
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}

}
