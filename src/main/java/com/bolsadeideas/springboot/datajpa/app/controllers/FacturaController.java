package com.bolsadeideas.springboot.datajpa.app.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Factura;
import com.bolsadeideas.springboot.datajpa.app.models.entity.ItemFactura;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Producto;
import com.bolsadeideas.springboot.datajpa.app.models.service.IClienteService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value = "/factura")
@SessionAttributes({ "factura" })
@Secured("ROLE_ADMIN")
@Slf4j
public class FacturaController {

	@Autowired
	private IClienteService clienteService;

	@GetMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Factura factura = clienteService.fetchByIdWithClienteWithItemFacturaWithProducto(id);// clienteService.findFacturaById(id);
		if (factura == null) {
			flash.addFlashAttribute("error", "La factura no existe en la base de datos");
			return "redirect:/listar";
		}
		model.addAttribute("factura", factura);
		model.addAttribute("titulo", "Factura: ".concat(factura.getDescripcion()));
		return "factura/ver";
	}

	@GetMapping(value = { "/form/{clienteId}" })
	public String crear(@PathVariable(value = "clienteId") Long clienteId, Model model, RedirectAttributes flash) {

		Cliente cliente = clienteService.findById(clienteId).orElse(null);
		if (cliente == null) {
			flash.addFlashAttribute("error", "¡El ciente no existe en la base de datos!");
			return "redirect:/listar";
		}
		Factura factura = new Factura();
		factura.setCliente(cliente);
		model.addAttribute("factura", factura);
		model.addAttribute("titulo", "Crear Factura");

		return "factura/form";
	}

	@GetMapping(value = { "/cargar-productos/{term}" }, produces = { "application/json" })
	public @ResponseBody List<Producto> cargarProductos(@PathVariable("term") String term) {
		return clienteService.findByName(term);
	}

	@PostMapping(value = "/form")
	public String guardar(@Valid Factura factura, BindingResult result, Model model,
			@RequestParam(name = "item_id[]", required = false) Long[] itemId,
			@RequestParam(name = "cantidad[]", required = false) Integer[] cantidad, RedirectAttributes flash,
			SessionStatus status) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Crear Factura");
			return "factura/form";
		}

		if (itemId == null || itemId.length == 0) {
			model.addAttribute("titulo", "Crear Factura");
			model.addAttribute("error", "Error: la factura NO puede no tener líneas!");
			return "factura/form";
		}
		for (int i = 0; i < itemId.length; i++) {
			Producto producto = clienteService.findProductoById(itemId[i]);
			ItemFactura linea = new ItemFactura();
			linea.setProducto(producto);
			linea.setCantidad(cantidad[i]);
			factura.addItemFactura(linea);

			log.info("ID: " + itemId[i].toString() + ", cantidad: " + cantidad[i].toString());
		}

		clienteService.saveFactura(factura);
		status.setComplete();

		flash.addFlashAttribute("success", "Factura creada con éxito");

		return "redirect:/ver/" + factura.getCliente().getId();
	}

	@GetMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		Factura factura = clienteService.findFacturaById(id);
		if (factura != null) {
			clienteService.deleteFactura(id);
			flash.addFlashAttribute("success", "Factura elimindada con éxito!");
			return "redirect:/ver/" + factura.getCliente().getId();
		}
		flash.addFlashAttribute("error", "La factura no existe en la base de datos, no se pudo eliminar");
		return "redirect:/listar";
	}

}
