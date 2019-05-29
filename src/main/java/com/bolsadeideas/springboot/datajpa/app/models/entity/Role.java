package com.bolsadeideas.springboot.datajpa.app.models.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
/*
 * Se genera un constraint o restriccion donde indicas que la combinacion de
 * user_id y authority es única
 */
@Table(name = "authorities", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "authority" }) })
@Data
@NoArgsConstructor
public class Role implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String authority;



}
