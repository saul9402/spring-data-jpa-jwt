package com.bolsadeideas.springboot.datajpa.app.auth.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTAthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public JWTAthenticationFilter(AuthenticationManager authenticationManager) {
		/*
		 * Aqui se agrega la url que servirá para autenticación, es con la que se deberá
		 * "iniciar sesión" y la que devolverá el jwt
		 */
		this.authenticationManager = authenticationManager;
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

		String username = ((User) authResult.getPrincipal()).getUsername();

		Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

		Claims claims = Jwts.claims();
		/*
		 * Se agregan los roles como un json dentro de los claims del token
		 */
		claims.put("authorities", new ObjectMapper().writeValueAsString(roles));

		/*
		 * La contraseña debe ser asi de larga puesto que el algoritmo de cifrado asi lo
		 * exige, de lo contrario no generá el token y es como si no te hubieras
		 * logeado, T_T
		 */
		String token = Jwts.builder().setClaims(claims).setSubject(authResult.getName()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3_600_000L * 4))
				.signWith(Keys.hmacShaKeyFor(
						"Alguna.Clave.Secreta.Para.Generar.Mi.JWT.Larguisima.Para.Que.Sea.Super.Super.Segura.No.Chingues.Que.Castre.Es.Este.Pedo.xD"
								.getBytes()),
						SignatureAlgorithm.HS512)
				.compact();

		/*
		 * Se debe poner la palabra Bearer antes del token que va en el encabezado... No
		 * sé porque pero asi es...
		 */
		response.addHeader("Authorization", "Bearer ".concat(token));

		/*
		 * Se agrega un cuerpo para el response el cual contendrá el token, los detalles
		 * del usuario y un mensaje, todo esto en formato json
		 */
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("token", token);
		body.put("user", (User) authResult.getPrincipal());
		body.put("mensaje", String.format("Hola %s has iniciado sesion con exito", username));

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
