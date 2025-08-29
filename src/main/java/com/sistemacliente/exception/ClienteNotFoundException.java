package com.sistemacliente.exception;

public class ClienteNotFoundException extends RuntimeException{
	public ClienteNotFoundException(Long id) {
		super("Cliente com o id = "+id+" não encontrado.");
	}
	
	public ClienteNotFoundException(String cpf) {
		super("Cliente com o CPF = "+cpf+" não encontrado.");
	}
	
	public ClienteNotFoundException() {
		super("Cliente não encontrado.");
	}
}
