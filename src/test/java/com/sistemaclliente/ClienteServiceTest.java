package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	/*Os CPF's tem oito dígitos mesmo que o esperado seja 11. Aqui não interfere em nossos testes. */
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
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarSalvarCliente_retonarDTO() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678");
		dto.setEmail("marcus@email.com");
		dto.setNome("Marcus");
		
		Cliente salvo = new Cliente(dto);
		salvo.setId(1L); //id não é gerado automaticamente.
		
		when(repository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
		when(repository.save(any(Cliente.class))).thenReturn(salvo);
		
		ClienteResponseDTO response = service.salvarCliente(dto);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCpf()).isEqualTo("12345678");
		assertThat(response.getNome()).isEqualTo("Marcus");
		
		verify(repository).save(any(Cliente.class));
		verify(repository).findByCpf(dto.getCpf());
		verifyNoMoreInteractions(repository);
		
	}
	
	@Test
	public void testaSalvarCliente_CpfJaExistente() {
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
		CpfJaCadastradoException ex = 
		assertThrows(CpfJaCadastradoException.class, () -> service.salvarCliente(dto));
		assertThat(ex.getMessage()).isEqualTo("O CPF 12345678 já está cadastrado.");
		verify(repository).findByCpf(dto.getCpf());
		verify(repository, never()).save(any(Cliente.class));
		verifyNoMoreInteractions(repository);
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
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscarClientePorId_naoEncontrarCliente() {
		when(repository.findById(3L)).thenReturn(Optional.empty());
		
		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class, ()-> service.buscarClientePorId(3L));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 3 não encontrado.");
		verify(repository).findById(3L);
		verifyNoMoreInteractions(repository);
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
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarDeletarClientePor_naoEncontrarCliente() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class, () -> service.deletarClientePorId(1L));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 1 não encontrado.");
		verify(repository).findById(1L);
		verify(repository, never()).delete(any(Cliente.class));
		verifyNoMoreInteractions(repository);
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
		when(repository.saveAndFlush(any(Cliente.class)))
		.thenAnswer(invocation -> invocation.getArgument(0));
		
		ClienteResponseDTO response = service.atualizarCliente(1L, dto);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getNome()).isEqualTo("Carlos");
		assertThat(response.getEmail()).isEqualTo("carlos@email.com");
		
		verify(repository).findById(1L);
		verify(repository).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarAltualizarCliente_naoEncontrarCliente() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail("carlos@email.com");
		
		when(repository.findById(1L)).thenReturn(Optional.empty());

		ClienteNotFoundException ex = 
				assertThrows(ClienteNotFoundException.class, () -> service.atualizarCliente(1L, dto));
		
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 1 não encontrado.");
		verify(repository).findById(1L);
		verifyNoMoreInteractions(repository);
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
		
		AlteracaoDeCpfException ex = assertThrows(AlteracaoDeCpfException.class, 
				() -> service.atualizarCliente(1L, dto));
		
		assertThat(ex.getMessage()).isEqualTo("Alteração de CPF não permitida.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
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
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarEncontrarPorCpf_naoEncontrarCliente() {
		when(repository.findByCpf("12345678")).thenReturn(Optional.empty());
		ClienteNotFoundException ex = 
				assertThrows(ClienteNotFoundException.class, () -> service.encontrarPorCpf("12345678"));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o CPF = "+"12345678"+" não encontrado.");
		verify(repository).findByCpf("12345678");
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarListaPaginada_retornarListaPaginadaDTO() {
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
		Page<Cliente> pageMock = new PageImpl<>(lista);
		
		PageRequest pageable = PageRequest.of(0, 2);
		when(repository.findAll(any(PageRequest.class))).thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = service.listaPaginada(0, 2);
		
		assertThat(page).isNotNull();
		assertThat(page.getContent()).hasSize(2);		
		assertThat(page.getContent()).extracting(ClienteResponseDTO::getCpf)
		.containsExactly("12345678", "87654321");

		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Antônio");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("12345678");
		assertThat(page.getContent().get(1).getCpf()).isEqualTo("87654321");
		
		verify(repository).findAll(pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarListaPaginadaPorOrdenacao_retornarLista() {
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
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		when(repository.findAll(any(PageRequest.class))).thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = service.listaPaginadaPorOrdenacao(0, 2, "nome");
		
		assertThat(page).isNotNull();
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getContent()).extracting(ClienteResponseDTO::getCpf)
		.containsExactly("12345678", "87654321");

		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Antônio");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("12345678");
		assertThat(page.getContent().get(1).getCpf()).isEqualTo("87654321");
		
		verify(repository).findAll(any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscarPorNome_retornarListaPaginadaPorNome() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Cliente cliente2 = new Cliente();
		cliente2.setId(2L);
		cliente2.setNome("Marcus Antônio");
		cliente2.setEmail("antonio@email.com");
		cliente2.setCpf("87654321");
		
		List<Cliente> lista = List.of(cliente1, cliente2);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2);
		
		when(repository.findByNomeContainingIgnoreCase("Marcus", pageable)).thenReturn(pageMock);
		Page<ClienteResponseDTO> page = service.buscarPorNome("Marcus", 0, 2);
		
		assertThat(page).isNotNull();
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getContent()).extracting(ClienteResponseDTO::getCpf)
		.containsExactly("12345678", "87654321");

		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus Vinicius");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Marcus Antônio");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("12345678");
		assertThat(page.getContent().get(1).getCpf()).isEqualTo("87654321");
		
		verify(repository).findByNomeContainingIgnoreCase("Marcus", pageable);
	}
	
	@Test
	public void testarAtualizarParcial_clienteNaoEncontrado() throws ClienteNotFoundException, 
	JsonMappingException{
		when(repository.findById(99L)).thenReturn(Optional.empty());
		Map<String, Object> updates = Map.of("nome", "Marcus");
		ClienteNotFoundException ex = 
				assertThrows(ClienteNotFoundException.class,() -> service.atualizarParcial(99L,updates));
		
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 99 não encontrado.");
		verify(repository).findById(99L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test
	public void testarAtualizarParcial_retornarDTO() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = Map.of("nome", "Antônio", "email", "antonio@email.com");
		Cliente atualizado = new Cliente();
		atualizado.setNome("Antônio");
		atualizado.setEmail("antonio@email.com");
		atualizado.setId(1L);
		atualizado.setCpf("12345678");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		when(mapper.updateValue(cliente1, updates)).thenReturn(atualizado);
		when(repository.saveAndFlush(atualizado)).thenReturn(atualizado);
		
		ClienteResponseDTO response = service.atualizarParcial(1L, updates);
		
		assertThat(response).isNotNull();
		assertThat(response.getNome()).isEqualTo("Antônio");
		assertThat(response.getEmail()).isEqualTo("antonio@email.com");
		assertThat(response.getId()).isEqualTo(1L);
		
		verify(repository).findById(1L);
		verify(repository).saveAndFlush(atualizado);
		verify(mapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper).updateValue(cliente1, updates);
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test 
	public void testarAtualizarParcialComId_retornarExececao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = Map.of("id", 2L, "email", "antonio@email.com");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		assertThat(e.getMessage()).isEqualTo("O campo id não pode ser alterado.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test
	public void testarAtualizarParcialAlterarCpf_retornarExcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = Map.of("cpf", "32165487");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		AlteracaoDeCpfException e = assertThrows(AlteracaoDeCpfException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("Alteração de CPF não permitida.");
		
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
		
	}
	
	@Test
	public void testarAtualizarParcialNomeNulo_retornarexcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", null);
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("Nome não pode ser vazio ou nulo.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testarAtualizarParcialNomeEmBranco_retornarexcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", " ");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("Nome não pode ser vazio ou nulo.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}
	
	/*cpf*/
	@Test
	public void testarAtualizarParcialEmailNulo_retornarexcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", null);
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("E-mail não pode ser vazio.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testarAtualizarParcialEmailEmBranco_retornarexcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", " ");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("E-mail não pode ser vazio.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarAtualizarParcialEmailFormatoInvalido_retornarexcecao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", "marcus.email");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("O formato do email está incorreto.");
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void testarBuscaEmailPaginadaOrdenada_retornarListaPaginada() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Cliente cliente2 = new Cliente();
		cliente2.setId(2L);
		cliente2.setNome("Marcus Antônio");
		cliente2.setEmail("marcus@email.com.br");
		cliente2.setCpf("87654321");
		
		List<Cliente> lista = List.of(cliente1, cliente2);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		
		when(repository.findByEmailContainingIgnoreCase("mar", pageable))
		.thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = 
				service.buscaEmailPaginadaOrdenada("mar", 0, 2, "nome");
		
		assertThat(page).isNotNull();
		assertThat(page.getContent().size()).isEqualTo(2);
		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus Vinicius");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Marcus Antônio");
		
		verify(repository).findByEmailContainingIgnoreCase("mar", pageable);
		verifyNoMoreInteractions(repository);
		
 	}
	
	@Test
	public void testarBuscaEmailPaginadaOrdenada_PaginaMenorQue0_retornarExcecao() {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> service.buscaEmailPaginadaOrdenada("marcus", -1, 0, "nome"));
		
		assertThat(ex.getMessage())
		.isEqualTo("Número da página não pode ser negativo e de itens por páginas menor que 1.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscaEmailPaginadaOrdenada_ItensMenorQue1_retornarExcecao() {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> service.buscaEmailPaginadaOrdenada("marcus", 0, 0, "nome"));
		
		assertThat(ex.getMessage())
		.isEqualTo("Número da página não pode ser negativo e de itens por páginas menor que 1.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscaEmailPaginadaOrdenada_EmailNulo_retornarExcecao() {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> service.buscaEmailPaginadaOrdenada(null, 0, 1, "nome"));
		
		assertThat(ex.getMessage()).isEqualTo("E-mail não pode ser vazio.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscaEmailPaginadaOrdenada_EmailVazio_retornarExcecao() {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> service.buscaEmailPaginadaOrdenada(" ", 0, 1, "nome"));
		
		assertThat(ex.getMessage()).isEqualTo("E-mail não pode ser vazio.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
}





















