package com.bolsadeideas.springboot.datajpa.app.auth.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import com.bolsadeideas.springboot.datajpa.app.auth.SimpleGrantedAuthorityMixin;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTServiceImpl implements JWTService {

	public static final String SECRET = Base64Utils.encodeToString(
			"Alguna.Clave.Secreta.Para.Generar.Mi.JWT.Larguisima.Para.Que.Sea.Super.Super.Segura.No.Chingues.Que.Castre.Es.Este.Pedo.xD"
					.getBytes());

	public static final Long EXPIRATION_DATE = 3_600_000L * 4;

	public static final String TOKEN_PREFIX = "Bearer ";

	public static final String HEADER_STRING = "Authorization";

	@Override
	public String create(Authentication authentication) throws IOException {
		String username = ((User) authentication.getPrincipal()).getUsername();

		Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();

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
		String token = Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_DATE))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS512).compact();
		return token;
	}

	@Override
	public boolean validate(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public Claims getClaims(String token) {
		Claims claims = (Claims) Jwts.parser().setSigningKey(SECRET.getBytes()).parse(resolve(token)).getBody();
		return claims;
	}

	@Override
	public String getUsername(String token) {
		/**
		 * Se obtiene el username que debe venir en el subject
		 */
		return getClaims(token).getSubject();
	}

	@Override
	public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException {
		/**
		 * Se obtienen los roles que se dejarón en los claims dentro de un atributo
		 * llamado: authorities
		 */
		Object roles = getClaims(token).get("authorities");
		/**
		 * Esos roles se dejarón como json dentro de los claims, hay que recuperarlos
		 * haciendo el proceso inverso convertir de string a objeto java y convertirlo a
		 * una lista
		 */
		Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
				/**
				 * Se crea un mixin ya que hay una propiedad llamada authority en el json dentro
				 * jwt que al intentar recuperar se rompe, ya que en la clase
				 * SimpleGrantedAuthority se llama role y no authority
				 */
				.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
				.readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));

		return authorities;
	}

	@Override
	public String resolve(String token) {
		if (!StringUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
			return token.replace(TOKEN_PREFIX, "");
		}
		return null;
	}

}
