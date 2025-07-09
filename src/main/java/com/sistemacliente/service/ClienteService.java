package com.sistemacliente.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistemacliente.model.Cliente;
import com.sistemacliente.repository.ClienteRepository;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	/*lista de todos os clientes*/
	public List<Cliente> listagemCliente (){
		return clienteRepository.findAll();
	}
	

	public Cliente adicionarCliente(Cliente cliente) {
		return clienteRepository.save(cliente);
	}
	
	public Cliente buscarClientePorId(Long id) {
		return clienteRepository.findById(id).get();
	}
	
}



