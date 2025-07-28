package com.sistemacliente.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.exception.AlteracaoDeCpfException;
import com.sistemacliente.exception.ArgumentoInvalidoException;
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
	
	@Autowired
	private ObjectMapper mapper;
	
	String regexEmail = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

	/* lista de todos os clientes */
	public List<ClienteResponseDTO> listagemCliente() {
		List<Cliente> lista = repository.findAll();
		return lista.stream().map(ClienteResponseDTO::new).toList();
	}

	public ClienteResponseDTO salvarCliente(ClienteRequestDTO dto) {
		
		if (repository.findByCpf(dto.getCpf()).isPresent()) {
			throw new CpfJaCadastradoException(dto.getCpf());
		}

		Cliente cliente = new Cliente(dto);
		Cliente salvo = repository.save(cliente); // Somente a entidade pode ser salva no banco.
		return new ClienteResponseDTO(salvo);
	}

	public ClienteResponseDTO buscarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		return new ClienteResponseDTO(clienteEncontrado);
	}

	public void deletarClientePorId(Long id) {
		Cliente clienteEncontrado = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		repository.delete(clienteEncontrado);
	}

	public ClienteResponseDTO atualizarCliente(Long id, ClienteRequestDTO dto) {
		Cliente clienteEncontrado = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));

		if (!clienteEncontrado.getCpf().equals(dto.getCpf())) {
			throw new AlteracaoDeCpfException();
		}

		clienteEncontrado.setNome(dto.getNome());
		clienteEncontrado.setEmail(dto.getEmail());
		return new ClienteResponseDTO(repository.saveAndFlush(clienteEncontrado));
	}

	public ClienteResponseDTO encontrarPorCpf(String cpf) {
		Cliente cliente = repository.findByCpf(cpf).orElseThrow(() -> new ClienteNotFoundException(cpf));
		return new ClienteResponseDTO(cliente);
	}

	public Page<ClienteResponseDTO> listaPaginada(int pagina, int itens) {
		PageRequest pageable = PageRequest.of(pagina, itens); // critério da página
		Page<Cliente> page = repository.findAll(pageable);
		return page.map(ClienteResponseDTO::new);
	}

	public Page<ClienteResponseDTO> listaPaginadaPorOrdenacao(int pagina, int itens, String ordenadoPor) {
		PageRequest pageable = PageRequest.of(pagina, itens, Sort.by(ordenadoPor).ascending());
		Page<Cliente> page = repository.findAll(pageable);
		return page.map(ClienteResponseDTO::new);
	}

	public Page<ClienteResponseDTO> buscarPorNome(String nome, int pagina, int itens) {
		PageRequest pageable = PageRequest.of(pagina, itens);
		Page<Cliente> page = repository.findByNomeContainingIgnoreCase(nome, pageable);
		return page.map(ClienteResponseDTO::new);
	}

	public ClienteResponseDTO atualizarParcial(Long id, Map<String, Object> updates) throws JsonMappingException {
		Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));

		if (updates.containsKey("id")) {
			throw new IllegalArgumentException("O campo id não pode ser alterado.");
		}

		if (updates.containsKey("cpf")) {
			throw new IllegalArgumentException("O campo CPF não pode ser alterado.");
		}

		if (updates.containsKey("nome")) {
			Object nome = updates.get("nome");
			if (nome == null || nome.toString().isBlank()) {
				throw new IllegalArgumentException("Nome não pode ser vazio.");
			}
		}
		
		if(updates.containsKey("email")) {
			Object email = updates.get("email");
			if(email == null || email.toString().isBlank()) {
				throw new IllegalArgumentException("E-mail não pode ser vazio.");
			}
			
			if(!email.toString().matches(regexEmail)) {
				throw new IllegalArgumentException("O formato do email está incorreto.");
			}
		}
		
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Cliente atualizado = mapper.updateValue(cliente, updates);
		Cliente novo = repository.saveAndFlush(atualizado);
		return new ClienteResponseDTO(novo);

	}
	
	public Page<ClienteResponseDTO> buscarPorEmail(String email, int pagina, int itens){
		PageRequest pageable = PageRequest.of(pagina, itens);
		Page<Cliente> page = repository.findByEmail(email, pageable);
		return page.map(ClienteResponseDTO::new);
	}
	
	public ClienteResponseDTO atualizarEmail(Long id, String email) {
		Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		
		if(email == null || email.isBlank()) {
			throw new IllegalArgumentException("E-mail não pode ser vazio.");
		}

		if(!email.matches(regexEmail)) {
			throw new IllegalArgumentException("Formato do e-mail inválido.");
		}
		
		cliente.setEmail(email);
		Cliente clienteAtualizado = repository.saveAndFlush(cliente); // atualiza no banco de dados
		return new ClienteResponseDTO(clienteAtualizado);
	}
	
	public ClienteResponseDTO atualizarNomeEmailParcial(Long id, Map<String, Object> updates) {
		Cliente cliente = repository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		
		if(updates.containsKey("id") || updates.containsKey("cpf")) {
			throw new IllegalArgumentException("Id e CPF, não será mudados aqui.");
		}
		
		if(updates.containsKey("nome")) {
			Object nome = updates.get("nome");
			
			if(nome == null || nome.toString().isBlank()) {
				throw new IllegalArgumentException("Nome não pode ser vazio");
			}
			cliente.setNome(nome.toString());
		}
		
		if(updates.containsKey("email")) {
			Object email = updates.get("email");
			if(email == null || email.toString().isBlank()) {
				throw new IllegalArgumentException("E-mail não pode ser vazio.");
			}
			
			
			if(!email.toString().matches(regexEmail)) {
				throw new IllegalArgumentException("O formato do e-mail não é válido.");
			}
			cliente.setEmail(email.toString());
			
		}
		repository.saveAndFlush(cliente);
		return new ClienteResponseDTO(cliente);
	}
	
	public Page<ClienteResponseDTO> buscaEmailPaginadaOrdenada(String email, int pagina, int itens, String ordenadoPor){
		
		if(pagina <0 || itens <1) {
			throw new IllegalArgumentException("Número da página ou de itens por páginas menor que 1.");
		}
		
		PageRequest pageable = PageRequest.of(pagina, itens, Sort.by(ordenadoPor.trim()).ascending());
		Page<Cliente> page = repository.findByEmailContainingIgnoreCase(email, pageable);
		return page.map(ClienteResponseDTO::new);
	}
	
}















