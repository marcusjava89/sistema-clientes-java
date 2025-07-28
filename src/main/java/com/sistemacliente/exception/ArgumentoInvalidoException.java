package com.sistemacliente.exception;

public class ArgumentoInvalidoException extends RuntimeException{
	
	public ArgumentoInvalidoException (){
		super("Erro em alguma entrada do cliente.");
	}
	
	public ArgumentoInvalidoException (String mensagem){
		super(mensagem);
	}
}
