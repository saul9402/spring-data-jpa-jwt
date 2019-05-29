package com.bolsadeideas.springboot.datajpa.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bolsadeideas.springboot.datajpa.app.auth.filter.JWTAthenticationFilter;
import com.bolsadeideas.springboot.datajpa.app.models.service.JpaUserDetailsService;

//esta anotacion es importante para habilitar el uso de anotaciones en los metodos y sustituye a la configuracion que se ve comentada aqui
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	/*
	 * @Autowired private LoginSuccessHandler successHandler;
	 */	

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private JpaUserDetailsService jpaUserDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests().antMatchers("/", "/css/**", "/js/**", "/images/**", "/listar**", "/locale").permitAll()
				/*
				 * Lo que está aqui se sustituye por las anotaciones @Secured y @PreAuthorize
				 * .antMatchers("/uploads/** ").hasAnyRole("USER")
				 * .antMatchers("/ver/**").hasAnyRole("USER")
				 * .antMatchers("/form/**").hasAnyRole("ADMIN").antMatchers("/eliminar/**").
				 * hasAnyRole("ADMIN") .antMatchers("/factura/**").hasAnyRole("ADMIN")
				 */
				.anyRequest().authenticated()
				// .and().formLogin().successHandler(successHandler).loginPage("/login")
				// .permitAll().and().logout().permitAll().and().exceptionHandling().accessDeniedPage("/error_403")
				// se deshabilita el uso de sesiones para poder usar rest
				.and()
				/*
				 * Aqui se agrega el filtro de jwt que tomará cada petición que se realice y
				 * verificará que las credenciales sean correctas. El authenticationManager() se
				 * hereda de WebSecurityConfigurerAdapter la clase de la que extiende. También
				 * se deshabilita el csrf ya que no será utilizado en este caso porque será
				 * sustituido por jwt. Y por ultimo el manejo de sesion se setea a stateless ya
				 * que no se usara la sesión para persistir al usuario, no es necesario hacerlo
				 * puesto que cada cliente tendrá su token y de querer realizar alguna
				 * transaccion deberá "presentarlo".
				 */
				.addFilter(new JWTAthenticationFilter(authenticationManager())).csrf().disable().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Autowired
	public void configurerGlobal(AuthenticationManagerBuilder builder) throws Exception {

		/**
		 * JPA AUTHENTICATION
		 * 
		 */

		builder.userDetailsService(jpaUserDetailsService).passwordEncoder(passwordEncoder);

		/**
		 * JDBC AUTHENTICATION
		 */
		/*
		 * builder.jdbcAuthentication() .dataSource(dataSource)
		 * .passwordEncoder(passwordEncoder)
		 * .usersByUsernameQuery("select username, password, enabled from users where username = ?"
		 * )
		 * .authoritiesByUsernameQuery("select u.username, a.authority from authorities a inner join users u on (a.user_id = u.id) where u.username = ?"
		 * );
		 */

		/**
		 * Inmemory authentication
		 */
		/* PasswordEncoder encoder = passwordEncoder; */
		/*
		 * Forma 1 UserBuilder users = User.builder().passwordEncoder(password -> {
		 * return encoder.encode(password); });
		 */

		/*
		 * Forma 2 UserBuilder users = User.builder().passwordEncoder(password ->
		 * encoder.encode(password));
		 */
		/*
		 * Forma 3 la más bonita, :3
		 */
		/* UserBuilder users = User.builder().passwordEncoder(encoder::encode); */

		/*
		 * builder.inMemoryAuthentication().withUser(users.username("admin").password(
		 * "12345").roles("ADMIN", "USER"))
		 * .withUser(users.username("andres").password("12345").roles("USER"));
		 */

	}

}
