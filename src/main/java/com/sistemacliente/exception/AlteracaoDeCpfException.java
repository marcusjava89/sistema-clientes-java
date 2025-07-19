package com.sistemacliente.exception;

public class AlteracaoDeCpfException extends RuntimeException{
	public AlteracaoDeCpfException() {
		super("Alteração de CPF não permitida.");
	}
}
