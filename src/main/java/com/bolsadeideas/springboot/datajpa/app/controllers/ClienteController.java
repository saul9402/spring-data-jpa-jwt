package com.bolsadeideas.springboot.datajpa.app.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;
import com.bolsadeideas.springboot.datajpa.app.models.service.IClienteService;
import com.bolsadeideas.springboot.datajpa.app.models.service.IUploadFileService;
import com.bolsadeideas.springboot.datajpa.app.util.paginator.PageRender;
import com.bolsadeideas.springboot.datajpa.app.view.xml.ClienteList;

import lombok.extern.slf4j.Slf4j;

@Controller
//con esto va a guardar en la sesion un atributo con el nombre cliente
@SessionAttributes({ "cliente" })
@Slf4j
public class ClienteController {

	@Autowired
	private IClienteService clienteServiceImpl;

	@Autowired
	private IUploadFileService uploadFileServiceImpl;

	@Autowired
	private MessageSource messageSource;

	@GetMapping(value = "/ver/{id}")
//	@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
	@PreAuthorize("hasRole('ROLE_USER')")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {

		Cliente cliente = clienteServiceImpl.fetchByIdWithFacturas(id);// clienteServiceImpl.findById(id).orElse(null);
		if (cliente == null) {
			flash.addFlashAttribute("error", "el cliente no existe en la base de datos");
			return "redirect:/listar";
		}
		model.addAttribute("cliente", cliente);
		model.addAttribute("titulo", "Detalle cliente: " + cliente.getNombre());
		return "ver";
	}

	@GetMapping(value = "/listar-rest")
	public @ResponseBody ClienteList listarRest() {
		/**
		 * Se debe poner asi ya que si es necesario usar xml se necesita un wrapper que
		 * contenga la lista de clientes, de lo contrario el xml se rompe. Al hacer esto
		 * en la URL, entonces, se debe mandar el parametro "?format=json" para renderizar
		 * json ya que por defecto será xml.
		 */
		return new ClienteList(clienteServiceImpl.findAll());
	}

	@RequestMapping(value = { "", "/", "/listar" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication, HttpServletRequest request, Locale locale) {
		if (authentication != null) {
			log.info("El username es: {}", authentication.getName());
		}

		// formas diferentes de obtener lo mismo
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (hasRole("ROLE_ADMIN")) {
			log.info("Hola {} tienes acceso", authentication.getName());
		}

		SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request,
				"ROLE_");

		if (securityContext.isUserInRole("ADMIN")) {
			log.info("USANDO WRAPPER El username es: {} ", authentication.getName());
		}

		if (request.isUserInRole("ROLE_ADMIN")) {
			log.info("USANDO SERVLET REQUEST El username es: {} ", authentication.getName());
		}

		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteServiceImpl.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<Cliente>("/listar", clientes);

		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null, locale));
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@GetMapping(value = { "/form" })
	@Secured("ROLE_ADMIN")
	public String crear(Model model) {
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("cliente", new Cliente());
		return "form";
	}

	@GetMapping(value = { "/form/{id}" })
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clienteServiceImpl.findById(id).orElse(null);
			if (cliente == null) {
				flash.addFlashAttribute("error", "El id del cliente no existe en la base de datos.");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El id del cliente no puede ser cero.");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Formulario de cliente.");
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@PostMapping(value = { "/form" })
	@Secured("ROLE_ADMIN")
	// si llegara a ser diferente el nombre de esta variable con respecto al nombre
	// que se dió en el metodo crear se deberá usar la anotacion
	// @ModelAttribute({nombre_con_que_se _mando} (sin llaves))
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus sessionStatus) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de cliente");
			return "form";
		}
		if (!foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadFileServiceImpl.delete(cliente.getFoto());
			}

			String uniqueFileName = null;
			try {
				uniqueFileName = uploadFileServiceImpl.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFileName + "'");
			cliente.setFoto(uniqueFileName);

		}

		String mensajeFlash = cliente.getId() != null ? "Cliente editado con éxito" : "Cliente creado con éxito";
		clienteServiceImpl.save(cliente);
		sessionStatus.setComplete();
		// se utiliza para crear los mensajes que aparecen y desaparecen con cada
		// petición
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

	@RequestMapping(value = { "/eliminar/{id}" })
	@Secured("ROLE_ADMIN")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			Cliente cliente = clienteServiceImpl.findById(id).orElse(null);
			clienteServiceImpl.deleteById(id);
			flash.addFlashAttribute("success", "Cliente eliminado con éxito");
			if (uploadFileServiceImpl.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con éxito");
			}
		}
		return "redirect:/listar";
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@GetMapping(value = "/uploads/{fileName:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String fileName) {
		Resource recurso = null;
		try {
			recurso = uploadFileServiceImpl.load(fileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	private boolean hasRole(String role) {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return false;
		}
		Authentication authentication = context.getAuthentication();
		if (authentication == null) {
			return false;
		}

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		return authorities.contains(new SimpleGrantedAuthority(role));

		/*
		 * for (GrantedAuthority authoritiy : authorities) { if
		 * (role.equals(authoritiy.getAuthority())) {
		 * System.out.println("Hola Usuario, ".concat(authentication.getName())
		 * .concat(" tu rol es: ".concat(authoritiy.getAuthority()))); return true; } }
		 * return false;
		 */

	}

}
