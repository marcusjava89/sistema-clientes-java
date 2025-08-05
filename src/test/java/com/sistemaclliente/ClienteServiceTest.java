package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.repository.ClienteRepository;
import com.sistemacliente.service.ClienteService;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

	@Mock
	private ClienteRepository repository;
	
	@Mock
	private ObjectMapper mapper;
	
	@InjectMocks
	private ClienteService service;
	
	@Test
	public void testarListagemCliente_retornarListaDTO() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Cliente cliente2 = new Cliente();
		cliente2.setId(2L);
		cliente2.setNome("Antônio");
		cliente2.setEmail("antonio@email.com");
		cliente2.setCpf("87654321");
		
		List<Cliente> lista = List.of(cliente1, cliente2);
		
		when(repository.findAll()).thenReturn(lista);
		
		List<ClienteResponseDTO> listaResponse = service.listagemCliente();
		
		assertThat(listaResponse).isNotNull();
		assertThat(listaResponse.size()).isEqualTo(2);
		assertThat(listaResponse.get(0).getNome()).isEqualTo("Marcus");
		assertThat(listaResponse.get(1).getNome()).isEqualTo("Antônio");
		assertThat(listaResponse.get(0).getCpf()).isEqualTo("12345678");
		assertThat(listaResponse.get(1).getCpf()).isEqualTo("87654321");
		
		verify(repository).findAll();		
	}
	
	@Test
	public void testaSalvarCliente_retonarDTO() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678");
		dto.setEmail("marcus@email.com");
		dto.setNome("Marcus");
		
		Cliente salvo = new Cliente(dto);
		salvo.setId(1L); //id não é gerado automaticamente.
		
		when(repository.save(any(Cliente.class))).thenReturn(salvo);
		
		ClienteResponseDTO response = service.salvarCliente(dto);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCpf()).isEqualTo("12345678");
		assertThat(response.getNome()).isEqualTo("Marcus");
		
	}

	@Test
	public void testarBuscarClientePorId_encontrarCliente() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		when(repository.findById(anyLong())).thenReturn(Optional.of(cliente1));
		
		ClienteResponseDTO response = service.buscarClientePorId(1L);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCpf()).isEqualTo("12345678");
		assertThat(response.getNome()).isEqualTo("Marcus");
		
		verify(repository).findById(1L);
	}
	
	@Test
	public void testarBuscarClientePorId_naoEncontrarCliente() {
		when(repository.findById(3L)).thenReturn(Optional.empty());
		
		assertThrows(ClienteNotFoundException.class, ()-> service.buscarClientePorId(3L));
		
		verify(repository).findById(3L);
	}
	
}





















