package com.bolsadeideas.springboot.datajpa.app.auth.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * Esta clase sera la encargada de revisar TODAS las peticiones que lleguen y
 * verificar que el jwt sea válido antes de realizar cualquier acción en la
 * aplicación.
 * 
 * @author Saul Avila
 *
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader("Authorization");

		if (!requieresAuthentication(header)) {
			chain.doFilter(request, response);
			return;
		}
		boolean tokenValid;
		Claims token = null;
		try {
			token = (Claims) Jwts.parser().setSigningKey(
					"Alguna.Clave.Secreta.Para.Generar.Mi.JWT.Larguisima.Para.Que.Sea.Super.Super.Segura.No.Chingues.Que.Castre.Es.Este.Pedo.xD"
							.getBytes())
					.parse(header.replace("Bearer ", "")).getBody();
			tokenValid = true;
		} catch (JwtException | IllegalArgumentException e) {
			tokenValid = false;
		}

		UsernamePasswordAuthenticationToken authentication = null;

		if (tokenValid) {
			/*
			 * Se obtiene el username que debe venir en el subject
			 */
			String username = token.getSubject();
			/*
			 * Se obtienen los roles que se dejarón en los claims dentro de un atributo
			 * llamado: authorities
			 */
			Object roles = token.get("authorities");
			/*
			 * Esos roles se dejarón como json dentro de los claims, hay que recuperarlos
			 * haciendo el proceso inverso convertir de string a objeto java y convertirlo a
			 * una lista
			 */
			Collection<? extends GrantedAuthority> authorities = Arrays
					.asList(new ObjectMapper().readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));

			authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	protected boolean requieresAuthentication(String header) {
		if (header == null || !header.toLowerCase().startsWith("Bearer ")) {
			return false;
		}
		return true;

	}

}
