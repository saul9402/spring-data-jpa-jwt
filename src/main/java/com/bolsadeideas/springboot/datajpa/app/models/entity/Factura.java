package com.bolsadeideas.springboot.datajpa.app.models.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

@Entity
@Table(name = "facturas")
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Factura implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotEmpty
	private String descripcion;
	private String observacion;

	@Temporal(TemporalType.DATE)
	private Date createAt;

	/**
	 * Se pone a LAZY para evitar que traiga al cliente en la consulta que trae a la
	 * factura. Traera solo al cliente cuando se mande a llamar a su método
	 * getCliente()
	 */
	/** Indica que tenemos muchas facturas asociadas a un solo cliente. */
	@ManyToOne(fetch = FetchType.LAZY)
	/**
	 * Con @XmlTransient omites este atributo cuando se serializa el xml
	 */
	@XmlTransient
	/**
	 * Con @JsonBackReference ayuda a que la relacion al hacer el json sea solo de
	 * cliente a factura. Indica que es la parte trasera o secundaria de la
	 * referencia
	 */
	@JsonBackReference
	private Cliente cliente;

	/**
	 * Una factura que tiene muchos itemsFactura
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	/**
	 * Al no ser bidireccional se indica el nombre de la llave fóranea que tendrá la
	 * tabla items_facturas, se llamará factura_id
	 */
	@JoinColumn(name = "factura_id")
	private List<ItemFactura> items;

	/**
	 * Este metodo se ejecutará justo antes de que se guarde cualquier factura
	 */
	@PrePersist
	public void prePersist() {
		createAt = new Date();
	}

	public Factura() {
		this.items = new ArrayList<ItemFactura>();
	}

	public void addItemFactura(ItemFactura item) {
		this.items.add(item);
	}

	public Double getTotal() {
		Double total = 0.0;
		int size = items.size();
		for (int i = 0; i < size; i++) {
			total += items.get(i).calcularImporte();
		}

		return total;
	}

}
