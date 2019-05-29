package com.bolsadeideas.springboot.datajpa.app.models.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bolsadeideas.springboot.datajpa.app.models.dao.IUsuarioDao;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Role;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Usuario;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JpaUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioDao usuarioDao;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioDao.findByUsername(username);

		if (usuario == null) {
			log.error("Error login: no existe el usuario: {}", username);
			throw new UsernameNotFoundException("Usuario no existe");
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (Role role : usuario.getRoles()) {
			log.info("Role: {}", role.getAuthority());
			authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
		}

		if (authorities.isEmpty()) {
			log.error("Error login: el usuario {} no tiene roles asignados", username);
			throw new UsernameNotFoundException("Usuario no tiene roles asignados");
		}

		return new User(username, usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
	}

}
