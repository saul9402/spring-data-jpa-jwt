package com.bolsadeideas.springboot.datajpa.app.view.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * Ya que la librería JAXB tiene problemas al serializar las listas se debe
 * configurar de esta forma, se crea un elemento root que contendrá una
 * propiedad que será la lista que deseas serializar y se anota con
 * XmlRootElement para que JAXB sepa que este es el elemento que contendrá
 * varios elementos.
 */
@XmlRootElement(name = "clientesList")
public class ClienteList {

//	@XmlElement(name = "cliente")
	private List<Cliente> clientes;

}
