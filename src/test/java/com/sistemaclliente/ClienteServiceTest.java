package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
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
import com.sistemacliente.exception.AlteracaoDeCpfException;
import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.exception.CpfJaCadastradoException;
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
	public void testarSalvarCliente_retonarDTO() {
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
	public void testaSalvarCliente_CPFJaExistente() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Carlos");
		cliente1.setEmail("carlos@email.com");
		cliente1.setCpf("12345678");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678");
		dto.setEmail("marcus@email.com");
		dto.setNome("Marcus");
		
		Cliente salvo = new Cliente(dto);
		salvo.setId(2L); //id não é gerado automaticamente.
		
		when(repository.findByCpf(dto.getCpf())).thenReturn(Optional.of(cliente1)); /*Retorna existente.*/
		assertThrows(CpfJaCadastradoException.class, () -> service.salvarCliente(dto));
		
		verify(repository).findByCpf(dto.getCpf());
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
	
	@Test
	public void testarDeletarClientePor_encontrarClienteDepoisDeletar() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		assertDoesNotThrow(() -> service.deletarClientePorId(1L));
		
		verify(repository).findById(1L);
		verify(repository).delete(cliente1);
	}
	
	@Test
	public void testarDeletarClientePor_naoEncontrarCliente() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(ClienteNotFoundException.class, () -> service.deletarClientePorId(1L));
		verify(repository).findById(1L);
	}
	
	@Test
	public void testarAltualizarCliente_encontrarRetornarDTO() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail("carlos@email.com");
		dto.setCpf(cliente1.getCpf());
	
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		when(repository.saveAndFlush(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		ClienteResponseDTO response = service.atualizarCliente(1L, dto);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getNome()).isEqualTo("Carlos");
		assertThat(response.getEmail()).isEqualTo("carlos@email.com");
		
		verify(repository).findById(1L);
		verify(repository).saveAndFlush(any(Cliente.class));
	}
	
	@Test
	public void testarAltualizarCliente_naoEncontrarCliente() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail("carlos@email.com");
		
		when(repository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ClienteNotFoundException.class, () -> service.atualizarCliente(1L, dto));
		verify(repository).findById(1L);
	}
	
	@Test
	public void testarAtualizarCliente_alteraCPF() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail("carlos@email.com");
		dto.setCpf("32165487");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		assertThrows(AlteracaoDeCpfException.class, () -> service.atualizarCliente(1L, dto));
		
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
	}
	
	@Test
	public void testarEncontrarPorCpf_encontrarCliente() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		when(repository.findByCpf("12345678")).thenReturn(Optional.of(cliente1));
		ClienteResponseDTO response = service.encontrarPorCpf("12345678");
			
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getNome()).isEqualTo("Marcus");
		assertThat(response.getEmail()).isEqualTo("marcus@email.com");
		assertThat(response.getCpf()).isEqualTo("12345678");
	
		verify(repository).findByCpf("12345678");
	}
	
	@Test
	public void testarEncontrarPorCpf_naoEncontrarCliente() {
		when(repository.findByCpf("12345678")).thenReturn(Optional.empty());

		assertThrows(ClienteNotFoundException.class, () -> service.encontrarPorCpf("12345678"));

		verify(repository).findByCpf("12345678");

	}
}





















