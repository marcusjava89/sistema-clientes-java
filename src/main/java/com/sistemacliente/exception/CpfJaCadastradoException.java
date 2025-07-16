package com.sistemacliente.exception;

public class CpfJaCadastradoException extends RuntimeException{
	
	public CpfJaCadastradoException(String cpf) {
		super("O CPF = "+cpf+" já está cadastrado");
	}
	
}
