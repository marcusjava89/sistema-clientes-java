package com.sistemacliente.exception;

public class EmailJaCadastradoException extends RuntimeException{
	public EmailJaCadastradoException() {
		super("E-mail indisponível para uso, está sendo utilizado por outro cliente.");
	}

}
