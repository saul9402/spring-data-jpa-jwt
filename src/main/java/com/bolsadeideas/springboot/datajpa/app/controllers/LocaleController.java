package com.bolsadeideas.springboot.datajpa.app.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LocaleController {

	/**
	 * Con este metodo se cambia el idioma y se redirige a la última url visitada
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping(value = "/locale")
	public String locale(HttpServletRequest request) {
		// referer entrega la referencia de la ultima url, el link de la ultima pagina
		String ultimaUrl = request.getHeader("referer");
		log.info("LA ULTIMA URL VISITADA ES: {}", ultimaUrl);
		return "redirect:".concat(ultimaUrl);
	}
}
