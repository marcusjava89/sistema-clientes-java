package com.sistemacliente.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sistemacliente.exception.AlteracaoDeCpfException;
import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.exception.CpfJaCadastradoException;
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

	public ClienteResponseDTO salvarCliente(ClienteRequestDTO dto) {
		
		if(repository.findByCpf(dto.getCpf()).isPresent()) {
			throw new CpfJaCadastradoException(dto.getCpf());
		}
		
		Cliente cliente = new Cliente(dto);
		Cliente salvo = repository.save(cliente); //Somente a entidade pode ser salva no banco.
		return new ClienteResponseDTO(salvo);
	}
	
	public ClienteResponseDTO buscarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		ClienteResponseDTO response= new ClienteResponseDTO(clienteEncontrado);
		return response;
	}
	
	public void deletarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id)
				.orElseThrow(() -> new ClienteNotFoundException(id));
		repository.delete(clienteEncontrado);
	}
	
	public ClienteResponseDTO atualizarCliente(Long id, ClienteRequestDTO dto) {
		Cliente clienteEncontrado = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		
		if(!clienteEncontrado.getCpf().equals(dto.getCpf())) {
			throw new AlteracaoDeCpfException();
		}
		
		clienteEncontrado.setNome(dto.getNome());
		clienteEncontrado.setEmail(dto.getEmail()); 
		return new ClienteResponseDTO(repository.saveAndFlush(clienteEncontrado));	
	}
	
	public ClienteResponseDTO encontrarPorCpf(String cpf) {
		Cliente cliente = repository.findByCpf(cpf)
				.orElseThrow(() -> new ClienteNotFoundException(cpf));
		return new ClienteResponseDTO(cliente);
	}
	
	public Page<ClienteResponseDTO> listaPaginada(int pagina, int itens){PageRequest pageable = PageRequest.of(pagina, itens);
		Page<Cliente> listaProdutos = repository.findAll(pageable);
		return listaProdutos.map(ClienteResponseDTO::new);
	}
	
	 public Page<ClienteResponseDTO> listaPaginadaPorOrdenacao(int pagina, int itens, String ordenadoPor) {
		 PageRequest pageable = PageRequest.of(pagina, itens, Sort.by(ordenadoPor).ascending());
		 Page<Cliente> lista = repository.findAll(pageable);
		 return lista.map(ClienteResponseDTO::new);
	 }
	 
	 public Page<ClienteResponseDTO> buscarPorNome(String nome, int pagina, int itens) {
		PageRequest pageable = PageRequest.of(pagina, itens);
		Page<Cliente> lista = repository.findByNomeContainingIgnoreCase(nome, pageable);
		return lista.map(ClienteResponseDTO::new);
	 }
	 
	
}












