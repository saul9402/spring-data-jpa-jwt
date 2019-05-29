package com.bolsadeideas.springboot.datajpa.app.models.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Factura;
import com.bolsadeideas.springboot.datajpa.app.models.entity.Producto;

public interface IClienteService {

	public List<Cliente> findAll();
	
	public Page<Cliente> findAll(Pageable pageable);

	public Cliente save(Cliente cliente);

	public Optional<Cliente> findById(Long id);

	public void deleteById(Long id);
	
	public List<Producto> findByName(String term);
	
	public void saveFactura(Factura factura);
	
	public Producto findProductoById(Long id);
	
	public Factura findFacturaById(Long id);
	
	public void deleteFactura(Long id);
	
	public Factura fetchByIdWithClienteWithItemFacturaWithProducto(Long id);
	
	public Cliente fetchByIdWithFacturas(Long id);
	
}
