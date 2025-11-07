package com.sistemacliente.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException;
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
	public ResponseEntity<ClienteResponseDTO> salvarCliente(@Valid @RequestBody ClienteRequestDTO dto){
		ClienteResponseDTO clienteNovo = service.salvarCliente(dto);
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
		return ResponseEntity.noContent().build(); // noContent(), retorno positivo não há conteúdo.
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
	
	@GetMapping(value = "/paginada")
	public ResponseEntity<Page<ClienteResponseDTO>> 
	listaPaginada(@RequestParam(defaultValue = "0") int pagina,
	@RequestParam(defaultValue = "3") int itens){
		Page<ClienteResponseDTO> page = service.listaPaginada(pagina, itens);
		return ResponseEntity.ok(page);
	}
	
	@GetMapping(value = "/paginadaordem")
	public ResponseEntity<Page<ClienteResponseDTO>> listaPaginadaOrdenada
	(@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "3") int itens, 
	@RequestParam(required = false) String ordenadoPor){
		Page<ClienteResponseDTO> lista = service.listaPaginadaPorOrdenacao(pagina, itens, ordenadoPor);
		return ResponseEntity.ok(lista);
	}
	
	@GetMapping(value = "/buscapornome")
	public ResponseEntity<Page<ClienteResponseDTO>> buscarPorNomePagina(
	@RequestParam(required = false) String nome, @RequestParam(defaultValue = "0") int pagina, 
	@RequestParam(defaultValue = "3") int itens){
		Page<ClienteResponseDTO> page = service.buscarPorNome(nome, pagina, itens);
		return ResponseEntity.ok(page);
	}
	
	@PatchMapping(value = "/parcial/{id}")
	public ResponseEntity<ClienteResponseDTO> atualizarParcial(@PathVariable Long id,
	@RequestBody Map<String, Object> updates) throws JsonMappingException{
		ClienteResponseDTO response = service.atualizarParcial(id, updates);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/buscaemail") public ResponseEntity<Page<ClienteResponseDTO>> buscaPorEmail
	(@RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int pagina, 
	@RequestParam(defaultValue = "3") int itens){
		Page<ClienteResponseDTO> page = service.buscarPorEmail(email, pagina, itens);
		return ResponseEntity.ok(page);
	}
	
	@PatchMapping(value = "/atualizaremail/{id}")
	public ResponseEntity<ClienteResponseDTO> atualizarEmail(@PathVariable Long id, 
	@RequestParam(required = false) String email){
		ClienteResponseDTO response= service.atualizarEmail(id, email);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/buscarporemail")
	public ResponseEntity<Page<ClienteResponseDTO>> buscarPorEmailOrdenada(
	@RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int pagina, 
	@RequestParam(defaultValue = "3") int itens, @RequestParam(required = false) String ordenadoPor){
		Page<ClienteResponseDTO> page = 
		service.buscaEmailPaginadaOrdenada(email, pagina, itens, ordenadoPor.trim());
		return ResponseEntity.ok(page);
	}
	
}