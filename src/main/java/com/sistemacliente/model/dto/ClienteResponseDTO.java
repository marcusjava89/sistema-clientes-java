package com.sistemacliente.model.dto;

import org.springframework.beans.BeanUtils;

import com.sistemacliente.model.Cliente;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClienteResponseDTO {

	public ClienteResponseDTO() {}

	public ClienteResponseDTO(Cliente cliente) {
		BeanUtils.copyProperties(cliente, this);
	}

	private Long id;
	private String nome;
	private String email;
	private String cpf;
}