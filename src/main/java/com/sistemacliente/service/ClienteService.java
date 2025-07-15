package com.sistemacliente.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.repository.ClienteRepository;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository repository;
	
	/*lista de todos os clientes*/
	public List<ClienteResponseDTO> listagemCliente (){
		List<Cliente> lista = repository.findAll();
		return lista.stream().map(ClienteResponseDTO::new).toList();
	}

	public ClienteResponseDTO adicionarCliente(ClienteRequestDTO dto) {
		Cliente cliente = new Cliente(dto);
		Cliente salvo = repository.save(cliente); //Somente a entidade pode ser salva no banco.
		return new ClienteResponseDTO(salvo);
	}
	
	public Cliente buscarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id)
				.orElseThrow(() -> new ClienteNotFoundException(id));
		
		return clienteEncontrado;
	}
	
	public void deletarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id)
				.orElseThrow(() -> new ClienteNotFoundException(id));
		
		repository.delete(clienteEncontrado);
	}
	
	public Cliente atualizarClienteService(Long id, Cliente cliente) {
		Cliente clienteEncontrado = repository.findById(id)
				.orElseThrow(() -> new ClienteNotFoundException(id));
		clienteEncontrado.setNome(cliente.getNome()); ;
		clienteEncontrado.setEmail(cliente.getEmail()); ;
		
		return repository.saveAndFlush(clienteEncontrado);	
	}
	
	
	
}





