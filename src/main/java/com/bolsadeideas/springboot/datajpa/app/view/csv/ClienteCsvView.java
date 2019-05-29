package com.bolsadeideas.springboot.datajpa.app.view.csv;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Cliente;

/*
 * Si requieres asociar mas de un controlador a una misma url deberás ponerle 
 * la extensión del contenido que genera ese componente y en el yml asociarlo 
 * a su Media Type, sino lo asocias es como si el componente no existiera ya 
 * que spring no sabe como asociar la vista con el componente
 */
@Component("listar.csv")
public class ClienteCsvView extends AbstractView {

	/*
	 * Se agrega el content-type que va a generar esta vista, :D
	 */
	public ClienteCsvView() {
		setContentType("text/csv");
	}

	/**
	 * Aqui deberas construir el csv que deseas mostrar o descargar
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		response.setHeader("Content-Disposition", "attachment; filename=\"clientes.csv\"");
		response.setContentType(getContentType());

		Page<Cliente> clientes = (Page<Cliente>) model.get("clientes");

		/*
		 * Con esto se escribirá el archivo csv
		 */
		ICsvBeanWriter csvBeanWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

		String[] header = { "id", "nombre", "apellido", "email", "createAt" };
		csvBeanWriter.writeHeader(header);

		/*
		 * No mms esto està muy puto fácil, xD. Sólo escribes el nombre de los headers y
		 * esta cosa los mapea con las propiedades del objeto que le mandes, D:
		 */

		for (Cliente cliente : clientes) {
			csvBeanWriter.write(cliente, header);
		}

		csvBeanWriter.close();

	}

	/**
	 * Con esto indicas que el contenido que se generará será descargable, por eso
	 * el "true"
	 */
	@Override
	protected boolean generatesDownloadContent() {
		return true;
	}

}
