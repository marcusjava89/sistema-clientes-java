package com.sistemacliente.exception;

public class ClienteNotFoundException extends RuntimeException{
	public ClienteNotFoundException(Long id) {
		super("Cliente com o id = "+id+" não encontrado.");
	}
}
