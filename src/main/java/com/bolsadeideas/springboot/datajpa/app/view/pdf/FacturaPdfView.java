package com.bolsadeideas.springboot.datajpa.app.view.pdf;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Factura;
import com.bolsadeideas.springboot.datajpa.app.models.entity.ItemFactura;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/*
 * Se le pone ese nombre al componente ya que será el mismo que se utilizará
 * para todas las vistas renderizadas lo unico que cambiara será la
 * implementación del metodo
 */
@Component("factura/ver")
/*
 * Podría implementar la interfaz View pero en este caso extenderemos de
 * AbstractPdfView
 */
public class FacturaPdfView extends AbstractPdfView {

	@Autowired
	private MessageSource messagesSource;

	@Autowired
	private LocaleResolver localeResolver;

	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		Locale locale = localeResolver.resolveLocale(request);
		/*
		 * Es un alternativa para I18N, el metodo getMessageSourceAccessor() viene de la
		 * clase padre
		 */
		MessageSourceAccessor mensajes = getMessageSourceAccessor();
		Factura factura = (Factura) model.get("factura");

		// el numero indica el numero de columnas, en este caso 1
		PdfPTable tabla = new PdfPTable(1);
		// tres filas para esa columna
		tabla.setSpacingAfter(20);
		/*
		 * Se crea la celda aparte para poder manipularla mejor, poner padding, cambiar
		 * la letra, color, etc... de no hacerse asi no sería posible maniuplar lo que
		 * está dentro de la celda
		 */
		PdfPCell cell = null;
		cell = new PdfPCell(new Phrase(messagesSource.getMessage("text.factura.ver.datos.cliente", null, locale)));
		cell.setBackgroundColor(new Color(184, 218, 255));
		cell.setPadding(8f);
		tabla.addCell(cell);

		tabla.addCell(factura.getCliente().getNombre() + " " + factura.getCliente().getApellido());
		tabla.addCell(factura.getCliente().getEmail());

		/*
		 * Otra tabla
		 */
		PdfPTable tabla2 = new PdfPTable(1);
		tabla2.setSpacingAfter(20);
		/*
		 * Con cuatro filas
		 */
		cell = new PdfPCell(new Phrase(messagesSource.getMessage("text.factura.ver.datos.factura", null, locale)));
		cell.setBackgroundColor(new Color(195, 230, 203));
		cell.setPadding(8f);
		tabla2.addCell(cell);
		tabla2.addCell(mensajes.getMessage("text.cliente.factura.folio") + ": " + factura.getId());
		tabla2.addCell(mensajes.getMessage("text.cliente.factura.descripcion") + ": " + factura.getDescripcion());
		tabla2.addCell(mensajes.getMessage("text.cliente.factura.fecha") + ": " + factura.getCreateAt());

		PdfPTable tabla3 = new PdfPTable(4);
		/*
		 * Se agregan medidas de relación respect a la columna que esta a lado
		 */
		tabla3.setWidths(new float[] { 3.5f, 1, 1, 1 });
		tabla3.addCell(mensajes.getMessage("text.factura.form.item.nombre"));
		tabla3.addCell(mensajes.getMessage("text.factura.form.item.precio"));
		tabla3.addCell(mensajes.getMessage("text.factura.form.item.cantidad"));
		tabla3.addCell(mensajes.getMessage("text.factura.form.item.total"));

		for (ItemFactura item : factura.getItems()) {
			tabla3.addCell(item.getProducto().getNombre());
			tabla3.addCell(item.getProducto().getPrecio().toString());
			cell = new PdfPCell(new Phrase(item.getCantidad().toString()));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			tabla3.addCell(cell);

			tabla3.addCell(item.calcularImporte().toString());
		}

		/*
		 * Se crea una celda donde ira el grant total
		 */
		cell = new PdfPCell(new Phrase(mensajes.getMessage("text.factura.form.total") + ":"));
		/*
		 * Se indica cuantas columnas va a ocupar
		 */
		cell.setColspan(3);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);

		tabla3.addCell(cell);
		tabla3.addCell(factura.getTotal().toString());

		document.add(tabla);
		document.add(tabla2);
		document.add(tabla3);

	}

}
