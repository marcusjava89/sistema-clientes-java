package com.sistemacliente.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClienteRequestDTO {

	public ClienteRequestDTO() {

	}
	
	@Size(min = 3, max = 60, message = "Nome deve ter entre 3 e 60 caracteres")
	@NotBlank(message = "Nome não pode ser vazio.")
	private String nome;
	
	@Email(message = "Formato inválido do e-mail.")
	@NotBlank(message = "E-mail não pode ser vazio.")
	private String email;
	
	@NotBlank(message = "CPF não pode ser vazio.")
	@Pattern(regexp = "\\d{11}", message = "Digite os 11 dígitos do CPF sem ponto e hífen.")
	private String cpf;

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





