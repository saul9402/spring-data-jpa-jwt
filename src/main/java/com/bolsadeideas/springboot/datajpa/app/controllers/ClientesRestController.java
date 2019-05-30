package com.bolsadeideas.springboot.datajpa.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bolsadeideas.springboot.datajpa.app.models.service.IClienteService;
import com.bolsadeideas.springboot.datajpa.app.view.xml.ClienteList;

/**
 * Se usa RestController en vez de Controller y ResponseBody, esto idica que la
 * clase solo contiene metodo REST
 * 
 * @author Saul Avila
 *
 */
@RestController
@RequestMapping(value = "/api/clientes")
public class ClientesRestController {

	@Autowired
	private IClienteService clienteServiceImpl;

	@GetMapping(value = { "/listar", "", "/" })
	/**
	 * Se autoriza solo a los admin y funciona, :')
	 * 
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	public ClienteList listar() {
		/**
		 * Se debe poner asi ya que si es necesario usar xml se necesita un wrapper que
		 * contenga la lista de clientes, de lo contrario el xml se rompe. Al hacer esto
		 * en la URL, entonces, se debe mandar el parametro "?format=json" para
		 * renderizar json ya que por defecto será xml.
		 */
		return new ClienteList(clienteServiceImpl.findAll());
	}

}
