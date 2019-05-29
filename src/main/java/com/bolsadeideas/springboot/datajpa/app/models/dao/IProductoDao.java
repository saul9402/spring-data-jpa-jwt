package com.bolsadeideas.springboot.datajpa.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bolsadeideas.springboot.datajpa.app.models.entity.Producto;

@Repository
public interface IProductoDao extends CrudRepository<Producto, Long> {

	@Query("SELECT p from Producto p WHERE p.nombre like %?1%")
	public List<Producto> findByName(String term);
	
	public List<Producto> findByNombreLikeIgnoreCase(String term);

}
