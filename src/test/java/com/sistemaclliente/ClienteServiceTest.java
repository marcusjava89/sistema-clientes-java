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

import java.awt.event.InvocationEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
import com.sistemacliente.exception.EmailJaCadastradoException;
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
	public void listagemCliente_retornarListaDTO() {
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
	public void listagemCliente_retornarListaVazia() {
		List<Cliente> lista = List.of();	
		when(repository.findAll()).thenReturn(lista);
		
		List<ClienteResponseDTO> response = service.listagemCliente();
		
		assertThat(response).isNotNull();
		assertThat(response).isEmpty();
		assertThat(response.size()).isEqualTo(0);
		
		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void salvarCliente_retonarDTO() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678");
		dto.setEmail("marcus@email.com");
		dto.setNome("Marcus");
		
		Cliente salvo = new Cliente(dto);
		salvo.setId(1L); //id não é gerado automaticamente.
		
		when(repository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
		when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
		when(repository.save(any(Cliente.class))).thenReturn(salvo);
		
		ClienteResponseDTO response = service.salvarCliente(dto);
		
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCpf()).isEqualTo("12345678");
		assertThat(response.getNome()).isEqualTo("Marcus");
		
		verify(repository).findByCpf(dto.getCpf());
		verify(repository).findByEmail(dto.getEmail());
		verify(repository).save(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void salvarCliente_CpfJaExistente_retornarExcecao() {
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
		
		when(repository.findByCpf(dto.getCpf())).thenReturn(Optional.of(cliente1)); 
		
		CpfJaCadastradoException ex = 
		assertThrows(CpfJaCadastradoException.class, () -> service.salvarCliente(dto));
		assertThat(ex.getMessage()).isEqualTo("O CPF 12345678 já está cadastrado.");
		
		verify(repository).findByCpf(dto.getCpf());
		verify(repository, never()).save(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void salvarCliente_emailExistente_retornarExcecao() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Carlos");
		cliente1.setEmail("carlos@email.com");
		cliente1.setCpf("12345678789");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678254");
		dto.setEmail("carlos@email.com");
		dto.setNome("Marcus");
		
		when(repository.findByEmail("carlos@email.com")).thenReturn(Optional.of(cliente1));
		
		EmailJaCadastradoException ex = assertThrows( EmailJaCadastradoException.class,
		() -> service.salvarCliente(dto));
		assertThat(ex.getMessage()).isEqualTo("E-mail indisponível, já está sendo utilizado.");

		verify(repository).findByCpf(dto.getCpf());
		verify(repository).findByEmail("carlos@email.com");
		verify(repository, never()).save(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "marcus.com"})
	public void salvarCliente_emailInvalido_retornarExecexao(String email) {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setCpf("12345678254");
		dto.setEmail(email);
		dto.setNome("Marcus");
		
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
		() -> service.salvarCliente(dto));
		
		assertThat(ex.getMessage()).isEqualTo("Formato inválido do e-mail.");
		
		verify(repository).findByCpf(dto.getCpf());
		verify(repository).findByEmail(email);
		verify(repository, never()).save(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void buscarClientePorId_sucesso_encontrarCliente() {
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
	public void buscarClientePorId_fracasso_naoEncontrarCliente() {
		when(repository.findById(3L)).thenReturn(Optional.empty());
		
		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class, ()-> service.buscarClientePorId(3L));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 3 não encontrado.");
		verify(repository).findById(3L);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void deletarClientePorId_sucesso_encontrarClienteDepoisDeletar() {
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
	public void deletarClientePor_fracasso_naoEncontrarCliente() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class, () -> service.deletarClientePorId(1L));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 1 não encontrado.");
		verify(repository).findById(1L);
		verify(repository, never()).delete(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarCliente_sucesso_retornarDTO() {
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
		verify(repository).findByEmail("carlos@email.com");
		verify(repository).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarCliente_naoEncontrarCliente() {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail("carlos@email.com");
		
		when(repository.findById(1L)).thenReturn(Optional.empty());

		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class, () -> service.atualizarCliente(1L, dto));
		
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 1 não encontrado.");
		
		verify(repository).findById(1L);
		verify(repository, never()).findByEmail("carlos@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarCliente_alterarCPF_retornaExcecao() {
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
		verify(repository, never()).findByEmail("carlos@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "carlos.com"})
	public void atualizarCliente_emailIvalido_retornaExcecao(String email) {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcu@email.com");
		cliente1.setCpf("41526487563");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos");
		dto.setEmail(email);
		dto.setCpf("41526487563");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		()-> service.atualizarCliente(1L, dto));
		
		assertThat(ex.getMessage()).isEqualTo("Formato inválido do e-mail.");
		
		verify(repository).findById(1L);
		verify(repository).findByEmail(email);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void atualizarCliente_emailExistente_retornaExcecao() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("41526487563");
		
		Cliente cliente2 = new Cliente();
		cliente1.setId(2L);
		cliente1.setNome("Carlos Flávio");
		cliente1.setEmail("carlos@email.com");
		cliente1.setCpf("85462398745");
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Carlos Jorge");
		dto.setEmail("carlos@email.com");
		dto.setCpf("41526487563");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		AlteracaoDeCpfException ex = assertThrows(AlteracaoDeCpfException.class,
		() -> service.atualizarCliente(1L, dto));
		
		assertThat(ex.getMessage()).isEqualTo("Alteração de CPF não permitida.");
		
		verify(repository).findById(1L);
		verify(repository, never()).findByEmail("carlos@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void encontrarPorCpf_sucesso_encontrarCliente() {
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
	public void encontrarPorCpf_fracasso_naoEncontrarCliente() {
		when(repository.findByCpf("12345678")).thenReturn(Optional.empty());
		
		ClienteNotFoundException ex = 
				assertThrows(ClienteNotFoundException.class, () -> service.encontrarPorCpf("12345678"));
		assertThat(ex.getMessage()).isEqualTo("Cliente com o CPF = "+"12345678"+" não encontrado.");
		
		verify(repository).findByCpf("12345678");
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void listaPaginada_sucesso_retornarListaPaginadaDTO() {
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
		PageRequest pageable = PageRequest.of(0, 2);
		Page<Cliente> pageMock = new PageImpl<>(lista);

		when(repository.findAll(any(PageRequest.class))).thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = service.listaPaginada(0, 2);
		
		assertThat(page).isNotNull();
		assertThat(page.getContent()).hasSize(2);		
		assertThat(page.getContent()).extracting(ClienteResponseDTO::getCpf)
		.containsExactly("12345678", "87654321");

		assertThat(page.getNumberOfElements()).isEqualTo(2);
		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Antônio");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("12345678");
		assertThat(page.getContent().get(1).getCpf()).isEqualTo("87654321");
		
		verify(repository).findAll(pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void listaPaginada_sucesso_retornarListaVazia() {
		List<Cliente> lista = List.of();
		PageRequest pageable = PageRequest.of(0, 2);
		Page pageMock = new PageImpl<>(lista);
		
		when(repository.findAll(pageable)).thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = service.listaPaginada(0, 2);
		
		assertThat(page.getContent()).isNotNull();
		assertThat(page.getNumberOfElements()).isEqualTo(0);
		
		verify(repository).findAll(pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@CsvSource({"-1 , 2" , "0, 0"})
	public void listaPaginada_paginaItensInvalidos_retornaExcecao(int pagina, int itens) {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		() -> service.listaPaginada(pagina, itens)); 
					
		assertThat(ex.getMessage())
		.isEqualTo("A página não pode ser negativa e itens não pode ser menor que 1.");
		
		verify(repository, never()).findAll(any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void listaPaginadaPorOrdenacao_sucesso_retornarListaCheia() {
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
	public void listaPaginadaPorOrdenacao_sucesso_retornarListaVazia() {
		List<Cliente> lista = List.of();
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome"));
		Page<Cliente> pageMock = new PageImpl<>(lista); 
		
		when(repository.findAll(pageable)).thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = service.listaPaginadaPorOrdenacao(0, 2, "nome");
		
		assertThat(page.getContent()).isNotNull();
		assertThat(page.getContent()).isEmpty();
		
		verify(repository).findAll(pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@CsvSource({"-1 , 2" , "0, 0"})
	public void listaPaginadaPorOrdenacao_paginaItensInvalidos_retornaExcecao(int pagina, int itens) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
		() -> service.listaPaginadaPorOrdenacao(pagina, itens, "nome"));
		
		assertThat(ex.getMessage())
		.isEqualTo("A página não pode ser negativa e itens não pode ser menor que 1.");
		
		verify(repository, never()).findAll(any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" "})
	public void listaPaginadaPorOrdenacao_ordenadoPorInvalido_retornaExcecao(String ordenadoPor) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
		() -> service.listaPaginadaPorOrdenacao(0, 2, ordenadoPor));
		
		assertThat(ex.getMessage()).isEqualTo("Critério de ordenação não pode ser vazio.");
		
		verify(repository, never()).findAll(any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void buscarPorNome_sucesso_retornarPageCheia() {
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
		PageRequest pageable = PageRequest.of(0, 2);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		
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
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void buscarPorNome_sucesso_retornarPageVazia() {
		List<Cliente> lista = List.of();
		PageRequest pageable = PageRequest.of(0, 2);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		
		when(repository.findByNomeContainingIgnoreCase("Marcus", pageable)).thenReturn(pageMock);
		Page<ClienteResponseDTO> page = service.buscarPorNome("Marcus", 0, 2);
		
		assertThat(page.getContent()).isNotNull();
		assertThat(page.getContent()).isEmpty();
		
		verify(repository).findByNomeContainingIgnoreCase("Marcus", pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" "})
	public void buscarPorNome_nomeInvalido_retornaExececao(String nome) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		() -> service.buscarPorNome(nome, 0, 2));
		
		assertThat(ex.getMessage()).contains("vazio").contains("nulo");
		
		verify(repository, never()).findByNomeContainingIgnoreCase(eq(nome), any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@CsvSource({"-1, 2", "0, 0"})
	public void buscarPorNome_paginaItensInvalidos_retornaExececao(int pagina, int itens) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		() -> service.buscarPorNome("Marcus", pagina, itens));
			
		assertThat(ex.getMessage()).contains("negativa").contains("menor que 1");
		
		verify(repository, never()).findByNomeContainingIgnoreCase(eq("Marcus"), any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarParcial_clienteNaoEncontrado() throws ClienteNotFoundException, 
	JsonMappingException{
		when(repository.findById(99L)).thenReturn(Optional.empty());
		Map<String, Object> updates = Map.of("nome", "Marcus");
		ClienteNotFoundException ex = 
		assertThrows(ClienteNotFoundException.class,() -> service.atualizarParcial(99L,updates));
		
		assertThat(ex.getMessage()).isEqualTo("Cliente com o id = 99 não encontrado.");
		verify(repository).findById(99L);
		verify(repository, never()).findByEmail(anyString());
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test
	public void atualizarParcial_sucesso_retornarDTO() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678111");
		
		Map<String, Object> updates = Map.of("nome", "Antônio", "email", "antonio@email.com");
		Cliente atualizado = new Cliente();
		atualizado.setNome("Antônio");
		atualizado.setEmail("antonio@email.com");
		atualizado.setId(1L);
		atualizado.setCpf("12345678625");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		when(mapper.updateValue(cliente1, updates)).thenReturn(atualizado);
		when(repository.saveAndFlush(atualizado)).thenReturn(atualizado);
		
		ClienteResponseDTO response = service.atualizarParcial(1L, updates);
		
		assertThat(response).isNotNull();
		assertThat(response.getNome()).isEqualTo("Antônio");
		assertThat(response.getEmail()).isEqualTo("antonio@email.com");
		assertThat(response.getId()).isEqualTo(1L);
		
		verify(repository).findById(1L);
		verify(repository).findByEmail(updates.get("email").toString());
		verify(repository).saveAndFlush(atualizado);
		verify(mapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper).updateValue(cliente1, updates);
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test 
	public void atualizarParcial_contemId_retornarExececao() throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678654");
		
		Map<String, Object> updates = Map.of("id", 2L, "email", "antonio@email.com");
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
		() -> service.atualizarParcial(1L, updates));
		assertThat(e.getMessage()).isEqualTo("O campo id não pode ser alterado.");
		verify(repository).findById(1L);
		verify(repository, never()).findByEmail("antonio@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(mapper);
	}
	
	@Test
	public void atualizarParcial_contemCpf_retornarExcecao() throws JsonMappingException {
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
		verify(repository, never()).findByEmail(anyString());
		verify(repository).findById(1L);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
		
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" "})
	public void atualizarParcial_nomeInvalido_retornarExcecao(String nome) throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", nome);
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
				() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("Nome não pode ser vazio ou nulo.");
		verify(repository).findById(1L);
		verify(repository, never()).findByEmail(anyString());
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "marcus.com", "@@@@@@@"})
	public void atualizarParcial_emailInvalido_retornarExcecao(String email) throws JsonMappingException {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678541");
        	
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", email);
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
		() -> service.atualizarParcial(1L, updates));
		
		assertThat(e.getMessage()).isEqualTo("Formato inválido do e-mail.");
		
		verify(repository).findById(1L);
		verify(repository, never()).findByEmail(email);
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarParcial_emailJaCadastrado_retornarExcecao() throws JsonMappingException {
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
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", "antonio@email.com");
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		when(repository.findByEmail("antonio@email.com")).thenReturn(Optional.of(cliente2));
		EmailJaCadastradoException ex = assertThrows(EmailJaCadastradoException.class,
		() -> service.atualizarParcial(1L, updates));
		
		assertThat(ex.getMessage()).isEqualTo("E-mail indisponível, já está sendo utilizado.");
		
		verify(repository).findById(1L);
		verify(repository).findByEmail("antonio@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verify(mapper, never()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		verify(mapper, never()).updateValue(any(Cliente.class), anyMap());
		verifyNoMoreInteractions(mapper);
		verifyNoMoreInteractions(repository);
		
	}
	
	@Test
	public void buscaPorEmail_sucesso_retornarPageCheia() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		List<Cliente> lista = List.of(cliente1);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2);
		
		when(repository.findByEmail("marcus@email.com",pageable)).thenReturn(pageMock);
		Page<ClienteResponseDTO> page = service.buscarPorEmail("marcus@email.com", 0, 2);
		
		assertThat(page.getContent()).isNotNull();
		assertThat(page.getContent()).isNotEmpty();
		assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus Vinicius");
		assertThat(page.getContent().get(0).getEmail()).isEqualTo("marcus@email.com");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("12345678");
		assertThat(page.getContent().size()).isEqualTo(1);
		
		verify(repository).findByEmail("marcus@email.com", pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void buscaPorEmail_naoEncontraCliente_retornarPageVazia() {
		List<Cliente> lista = List.of();
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2);
		
		when(repository.findByEmail("marcus@email.com", pageable)).thenReturn(pageMock);
		Page<ClienteResponseDTO> page = service.buscarPorEmail("marcus@email.com", 0, 2);
		
		assertThat(page.getContent()).isNotNull();
		assertThat(page.getContent()).isEmpty();
		
		verify(repository).findByEmail("marcus@email.com", pageable);
		verifyNoMoreInteractions(repository);
	}
	
	@ParameterizedTest
	@CsvSource({"-1, 2", "0, 0"})
	public void buscaPorEmail_paginaItensInvalidos_retornaExcecao(int pagina, int itens) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		() -> service.buscarPorEmail("marcus@email.com", pagina, itens));
		
		assertThat(ex.getMessage()).contains("negativa").contains("menor que 1");
		
		verify(repository, never()).findByEmail(anyString(), any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "marcus.com.br", "@@@"})
	public void buscarPorEmail_emailInvalido_retornaExcecao(String email) {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
		() -> service.buscarPorEmail(email, 0, 2));
		
		assertThat(ex.getMessage()).contains("inválido");
		
		verify(repository, never()).findByEmail(anyString(), any(PageRequest.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarEmail_sucesso_emailAtualizado() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678514");
		
		String email = "vinicius@email.com";
		
		when(repository.findById(1L)).thenReturn(Optional.of(cliente1));
		when(repository.findByEmail(email)).thenReturn(Optional.empty());
		cliente1.setEmail(email);
		when(repository.saveAndFlush(cliente1)).thenAnswer(invocation -> invocation.getArgument(0));
		
		ClienteResponseDTO atualizado = service.atualizarEmail(1L, email);
		
		assertThat(atualizado).isNotNull();
		assertThat(atualizado.getCpf()).isEqualTo(cliente1.getCpf());
		assertThat(atualizado.getEmail()).isEqualTo(cliente1.getEmail());
		assertThat(atualizado.getNome()).isEqualTo(cliente1.getNome());
		assertThat(atualizado.getId()).isEqualTo(cliente1.getId());
		
		verify(repository).findById(1L);
		verify(repository).findByEmail(email);
		verify(repository).saveAndFlush(cliente1);
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void atualizarEmail_clienteNaoEncontrado() {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		
		ClienteNotFoundException ex = assertThrows(ClienteNotFoundException.class,
		() -> service.atualizarEmail(99L, "vinicius@email.com"));
		
		assertThat(ex.getMessage()).contains("não encontrado");
		
		verify(repository).findById(99L);
		verify(repository, never()).findByEmail("vinicius@email.com");
		verify(repository, never()).saveAndFlush(any(Cliente.class));
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void buscaEmailPaginadaOrdenada_retornarListaPaginada() {
		Cliente cliente1 = new Cliente();
		cliente1.setId(1L);
		cliente1.setNome("Marcus Vinicius");
		cliente1.setEmail("marcus@email.com");
		cliente1.setCpf("12345678");
		
		Cliente cliente2 = new Cliente();
		cliente2.setId(2L);
		cliente2.setNome("Marcus Antônio");
		cliente2.setEmail("marcus@email.com");
		cliente2.setCpf("87654321");
		
		List<Cliente> lista = List.of(cliente1, cliente2);
		Page<Cliente> pageMock = new PageImpl<>(lista);
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		
		when(repository.findByEmailContainingIgnoreCase("marcus@email.com", pageable))
		.thenReturn(pageMock);
		
		Page<ClienteResponseDTO> page = 
		service.buscaEmailPaginadaOrdenada("marcus@email.com", 0, 2, "nome");
		
		assertThat(page).isNotNull();
		assertThat(page.getContent().size()).isEqualTo(2);
		assertThat(page.getContent().get(0).getNome()).isEqualTo("Marcus Vinicius");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Marcus Antônio");
		
		verify(repository).findByEmailContainingIgnoreCase("marcus@email.com", pageable);
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
		
		assertThat(ex.getMessage()).isEqualTo("Formato inválido do e-mail.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
	@Test
	public void testarBuscaEmailPaginadaOrdenada_EmailVazio_retornarExcecao() {	
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
				() -> service.buscaEmailPaginadaOrdenada(" ", 0, 1, "nome"));
		
		assertThat(ex.getMessage()).isEqualTo("Formato inválido do e-mail.");
		
		verify(repository, never()).findByEmailContainingIgnoreCase(anyString(), any());
		verifyNoMoreInteractions(repository);
	}
	
}





















