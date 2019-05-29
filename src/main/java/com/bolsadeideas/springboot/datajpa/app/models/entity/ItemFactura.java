package com.bolsadeideas.springboot.datajpa.app.models.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facturas_items")
@Data
@NoArgsConstructor
public class ItemFactura implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer cantidad;

	/**
	 * Indica que muchos items factura pueden pertenecer a un mismo producto
	 */
	/**
	 * Se creara en esta tabla (facturas_items) la llave foranea de producto y se
	 * especifica su nombre. Aunque se puede omitir.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "producto_id")
	/**
	 *@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) ayudará para evitar poner a EAGER el atributo 
	 */
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Producto producto;

	public Double calcularImporte() {
		return cantidad.doubleValue() * producto.getPrecio();
	}



}
