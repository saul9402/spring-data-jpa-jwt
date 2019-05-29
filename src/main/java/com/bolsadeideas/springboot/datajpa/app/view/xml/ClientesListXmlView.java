package com.bolsadeideas.springboot.datajpa.app.view.xml;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.xml.MarshallingView;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;

@Component("listar.xml")
public class ClientesListXmlView extends MarshallingView {

	@Autowired
	public ClientesListXmlView(Jaxb2Marshaller jaxb2Marshaller) {
		super(jaxb2Marshaller);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		model.remove("titulo");
		model.remove("page");

		Page<Cliente> clientes = (Page<Cliente>) model.get("clientes");

		model.remove("clientes");
		model.put("clientesList", new ClienteList(clientes.getContent()));

		super.renderMergedOutputModel(model, request, response);
	}

}
