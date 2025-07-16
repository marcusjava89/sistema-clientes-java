package com.sistemacliente.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.service.ClienteService;

import jakarta.validation.Valid;

@RestController
public class ClienteController {

	@Autowired
	private ClienteService service;
	
	@GetMapping(value = "/listarclientes")
	public ResponseEntity<List<ClienteResponseDTO>> listarClientes(){
		List<ClienteResponseDTO> listaDeClientes = service.listagemCliente();
		return ResponseEntity.ok(listaDeClientes);
	}
	
	@PostMapping(value = "/salvarcliente")
	public ResponseEntity<ClienteResponseDTO> 
	salvarCliente(@Valid @RequestBody ClienteRequestDTO dto){
		ClienteResponseDTO clienteNovo = service.adicionarCliente(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteNovo);
	}
	
	@GetMapping(value = "/encontrarcliente/{id}")
	public ResponseEntity<ClienteResponseDTO> encontrarClientePorId(@PathVariable Long id){
		ClienteResponseDTO encontrado = service.buscarClientePorId(id);
		return ResponseEntity.ok(encontrado);
	}
	
	@DeleteMapping(value = "/deletarporid/{id}")
	public ResponseEntity<Void> deletarClientePorId(@PathVariable Long id){
		service.deletarClientePorId(id);
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping(value = "/clientes/{id}")
	public ResponseEntity<ClienteResponseDTO> 
	atualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto){
		ClienteResponseDTO response = service.atualizarCliente(id, dto);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/clientecpf/{cpf}")
	public ResponseEntity<ClienteResponseDTO> encontrarClientePorCpf(@PathVariable String cpf){
		ClienteResponseDTO response = service.encontrarPorCpf(cpf);
		return ResponseEntity.ok(response);
	}
	
}




