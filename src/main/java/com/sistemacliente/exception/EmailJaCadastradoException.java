package com.sistemacliente.exception;

public class EmailJaCadastradoException extends RuntimeException{
	public EmailJaCadastradoException() {
		super("E-mail indisponível, já está sendo utilizado.");
	}

}
