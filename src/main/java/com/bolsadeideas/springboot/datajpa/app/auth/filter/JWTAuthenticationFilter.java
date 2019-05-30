package com.bolsadeideas.springboot.datajpa.app.auth.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bolsadeideas.springboot.datajpa.app.auth.service.JWTService;
import com.bolsadeideas.springboot.datajpa.app.auth.service.JWTServiceImpl;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Este filtro sólo se ejecutará cuando se apunte a la URL definida en el
 * contructor y sirve, básicamente, para crear el jwt de acceso a la aplicación.
 * 
 * @author Saul Avila
 *
 */
@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	/**
	 * Se crea una propiedad que implementara el jwtService y esta se hará llegar
	 * por contructor desde la configuracion de seguridad en spring
	 */
	private JWTService jwtService;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTService jwtService) {
		/*
		 * Aqui se agrega la url que servirá para autenticación, es con la que se deberá
		 * "iniciar sesión" y la que devolverá el jwt
		 */
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		/*
		 * Con esto se obtienen las credenciales que el usuario ingreso al hacer su
		 * request
		 */
		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username != null && password != null) {
			log.info("username desde request parameter (form-data) {}", username);
			log.info("password desde request parameter (form-data) {}", password);
		} else {
			Usuario user = null;
			try {
				/*
				 * En el input stream es donde vienen los datos del json enviado, :3
				 */
				user = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);

				username = user.getUsername();
				password = user.getPassword();

				log.info("username desde request InputStream (raw) {}", username);
				log.info("password desde request InputStream (raw) {}", password);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
		/*
		 * Este es el que permitirá la utenticación con jwt. Este es el que utilizará la
		 * conexión a la base de datos para verificar las credenciales del usuario, una
		 * vez verificado pasará al siguiente método successfulAuthentication, el cual
		 * generara el token y lo devolverá al usuario en su response.
		 */
		return authenticationManager.authenticate(authToken);
	}

	/**
	 * Este metodo maneja la petición cuando la autenticación fue exitosa
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		String token = jwtService.create(authResult);

		/*
		 * Se debe poner la palabra Bearer antes del token que va en el encabezado... No
		 * sé porque pero asi es...
		 */
		response.addHeader(JWTServiceImpl.HEADER_STRING, JWTServiceImpl.TOKEN_PREFIX.concat(token));

		/*
		 * Se agrega un cuerpo para el response el cual contendrá el token, los detalles
		 * del usuario y un mensaje, todo esto en formato json
		 */
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("token", token);
		body.put("user", (User) authResult.getPrincipal());
		body.put("mensaje", String.format("Hola %s has iniciado sesion con exito", authResult.getName()));

		/*
		 * El json creado se "escribe" en el response para eso se usa el metodo write
		 */
		response.getWriter().write(new ObjectMapper().writeValueAsString(body));
		response.setStatus(200);
		response.setContentType("application/json");
	}

	/**
	 * Este metodo manejara cuando la petición de autenticación no sea exitosa
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("mensaje", "Error de autenticacion, username o password no son validos");
		body.put("error", failed.getMessage());

		response.getWriter().write(new ObjectMapper().writeValueAsString(body));
		response.setStatus(401);
		response.setContentType("application/json");
		// se debe quitar el super ya que de no hacerlo la implementación que yo hago se
		// pierde
		// super.unsuccessfulAuthentication(request, response, failed);
	}

}
