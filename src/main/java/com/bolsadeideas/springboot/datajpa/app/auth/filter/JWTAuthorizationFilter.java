package com.bolsadeideas.springboot.datajpa.app.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.bolsadeideas.springboot.datajpa.app.auth.service.JWTService;
import com.bolsadeideas.springboot.datajpa.app.auth.service.JWTServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * Esta clase sera la encargada de revisar TODAS las peticiones que lleguen y
 * verificar que el jwt sea válido antes de realizar cualquier acción en la
 * aplicación.
 * 
 * @author Saul Avila
 *
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	/**
	 * Se crea una propiedad que implementara el jwtService y esta se hará llegar
	 * por contructor desde la configuracion de seguridad en spring
	 */
	private JWTService jwtService;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTService jwtService) {
		super(authenticationManager);
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader(JWTServiceImpl.HEADER_STRING);
		log.info("¡ENTRE EN EL FILTRO!");
		if (!requieresAuthentication(header)) {
			chain.doFilter(request, response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = null;

		if (jwtService.validate(header)) {
			authentication = new UsernamePasswordAuthenticationToken(jwtService.getUsername(header), null,
					jwtService.getRoles(header));
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	protected boolean requieresAuthentication(String header) {
		if (header == null || !header.startsWith(JWTServiceImpl.TOKEN_PREFIX)) {
			return false;
		}
		return true;

	}

}
