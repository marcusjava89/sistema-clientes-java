package com.sistemacliente.model.dto;

import org.springframework.beans.BeanUtils;

import com.sistemacliente.model.Cliente;

public class ClienteResponseDTO {

	public ClienteResponseDTO() {

	}
	
	public ClienteResponseDTO(Cliente cliente) {
		if(cliente != null) {			
			BeanUtils.copyProperties(cliente, this);
		}
	}
	
	private Long id;
	private String nome;
	private String email;
	private String cpf;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}	
}