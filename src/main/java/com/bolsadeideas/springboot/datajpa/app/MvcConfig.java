package com.bolsadeideas.springboot.datajpa.app;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	/*
	 * Cifrador para las contraseñas
	 */
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
	 * WebMvcConfigurer.super.addResourceHandlers(registry); // con esto se mapea
	 * todo lo que està haciendo referencia a /uploads/ a una ruta // externa que en
	 * este caso seria file:/C:/Temp/uploads/ (se configura como // recurso
	 * estatico) // toUri, agrega el esquema "file:" a la ruta String resourcePath =
	 * Paths.get("uploads").toAbsolutePath().toUri().toString();
	 * registry.addResourceHandler("/uploads/**").addResourceLocations(resourcePath)
	 * ; }
	 */

	/*
	 * VISTA DE ERROR PARA ACCESO DENEGADO
	 */
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/error_403").setViewName("error_403");

	}

	/*
	 * INTERNACIONALIZACION
	 */

	/**
	 * Con este bean se configura el idioma por defecto que se utilizará en la
	 * aplicación
	 * 
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
		// se pone el idioma y despues el país es_ES
		sessionLocaleResolver.setDefaultLocale(new Locale("es", "ES"));
		return sessionLocaleResolver;
	}

	/**
	 * Aqui es donde se crea el interceptor que procesara el idioma segpun un
	 * parametro llamado lang, por defecto el idioma será español
	 * 
	 * @return
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}

	/**
	 * Se agrega el interceptor creado para procesaor el idioma
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
	/*
	 * XML JAXB CONFIGURATION
	 */

	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		/*
		 * Aqui se ponen las clases "raíz" que serán convertidas a xml
		 */
		marshaller.setClassesToBeBound(
				new Class[] { com.bolsadeideas.springboot.datajpa.app.view.xml.ClienteList.class });
		return marshaller;
	}

}
