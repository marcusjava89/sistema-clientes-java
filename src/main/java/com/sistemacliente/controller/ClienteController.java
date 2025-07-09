package com.sistemacliente.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sistemacliente.model.Cliente;
import com.sistemacliente.service.ClienteService;

import jakarta.validation.Valid;

@RestController
public class ClienteController {

	@Autowired
	private ClienteService clienteService;
	
	@GetMapping(value = "/listarClientes")
	public ResponseEntity<List<Cliente>> listarClientes(){
		
		List<Cliente> listaDeClientes = clienteService.listagemCliente();
		return ResponseEntity.ok(listaDeClientes);
	}
	
	@PostMapping(value = "/salvarCliente")
	public ResponseEntity<Cliente> salvarCliente(@Valid @RequestBody Cliente cliente){
		Cliente clienteNovo = clienteService.adicionarCliente(cliente);
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteNovo);
	}
	
	@GetMapping(value = "/cliente/{id}")
	public ResponseEntity<Cliente> encontrarClientePorId(@PathVariable Long id){
		Cliente cliente = clienteService.buscarClientePorId(id);
		return ResponseEntity.ok(cliente);
	}
	
}




