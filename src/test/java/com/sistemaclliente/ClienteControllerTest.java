package com.sistemaclliente;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.controller.ClienteController;
import com.sistemacliente.exception.AlteracaoDeCpfException;
import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.exception.CpfJaCadastradoException;
import com.sistemacliente.exception.EmailJaCadastradoException;
import com.sistemacliente.exception.ValidationExceptionHandler;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.service.ClienteService;

/*Podemos fazer testes em conjunto para deixar a classe mais enxuta como o caso de testar verbo http in-
 *correto. Porém quando esses testes foram escritos não rodaram fazendo que tenha ser feito caso a caso.
 *Sempre que possível teste em grupo foram feitos.*/

@WebMvcTest(controllers = ClienteController.class)
@Import(ValidationExceptionHandler.class)
public class ClienteControllerTest {
	
	private ClienteResponseDTO cliente1;
	private ClienteResponseDTO cliente2;
	private ClienteRequestDTO dto;  
	
	@BeforeEach
	public void setup() {
		cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
	}
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@MockitoBean
	private ClienteService service;
	
	@Test
	@DisplayName("Retorna 200 e lista de todos os clientes do banco de dados.")
	public void listarClientes_listaCheia_retornar200() throws Exception {
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		when(service.listagemCliente()).thenReturn(lista);
		
		mvc.perform(get("/listarclientes")).andExpect(status().isOk())
		.andExpect(jsonPath("$[0].id").value(1)).andExpect(jsonPath("$[1].id").value(2))
		.andExpect(jsonPath("$[0].nome").value("Marcus"))
		.andExpect(jsonPath("$[1].nome").value("Antonio")).andExpect(jsonPath("$.length()").value(2));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listarClientes_listaVazia_retorno200() throws Exception {
		List<ClienteResponseDTO> lista = List.of();
		when(service.listagemCliente()).thenReturn(lista);
		
		mvc.perform(get("/listarclientes")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty())
		.andExpect(jsonPath("$").isArray()).andExpect(content().json("[]"));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listarClientes_retornao500() throws Exception {
		when(service.listagemCliente()).thenThrow(new RuntimeException());
		
		mvc.perform(get("/listarclientes")).andExpect(status().isInternalServerError())
		.andExpect(content().string(containsString("Erro")));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarClientes_verboHttpIncorreto_retorno405() throws Exception {	
		mvc.perform(get("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "POST"));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Returns 201 when saving DTO client.")
	public void salvarCliente_sucesso_retorno201() throws Exception {
		when(service.salvarCliente(any(ClienteRequestDTO.class))).thenReturn(cliente1);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).salvarCliente(any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "ab", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678901"})
	@DisplayName("Returns 400 when tries to save a client with an invalid name. A name is invalid if it"
	+" is empty, null, have  less then 3 characters or more then 60 characters.")
	public void salvarCliente_nomeInvalido_retorno400(String nome) throws Exception {
		dto.setNome(nome);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.nome")
		.value("Nome deve ter entre 3 e 60 caracteres, não pode ser nulo ou vazio."));
		
		verify(service, never()).salvarCliente(any());
		verifyNoMoreInteractions(service);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"com", " ", "marcus@marcus@"})
	@DisplayName("Returns 400 when trying to save a client with an invalid email adress.")
	public void salvarCliente_emailInvalido_retorno400(String email) throws Exception {
		dto.setEmail(email);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.email").value(containsString("inválido")));

		verify(service, never()).salvarCliente(any());
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "101089757er", "101089757", "25013569874965"})
	@DisplayName("Returns 400, tries to save a client with an invalid.")
	public void salvarCliente_cpfInvalido_retornar400(String cpf) throws Exception {
		dto.setCpf(cpf);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value(containsString("11 dígitos do CPF")));
		
		verify(service, never()).salvarCliente(any());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Returns 409, tries to save a client with an existing CPF.")
	public void salvarCliente_cpfExistente_retornar409() throws Exception {
		ClienteRequestDTO dto1 = new ClienteRequestDTO();
		dto1.setNome("Carlos");
		dto1.setCpf("23501206586");
		dto1.setEmail("carlos@gmail.com");
		
		when(service.salvarCliente(any(ClienteRequestDTO.class)))
		.thenThrow(new CpfJaCadastradoException("23501206586"));
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto1))).andExpect(status().isConflict())
		.andExpect(content().string("O CPF 23501206586 já está cadastrado."));
		
		verify(service).salvarCliente(any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_erroDeServidor_retrno500() throws Exception{
		when(service.salvarCliente(any(ClienteRequestDTO.class))).thenThrow(new RuntimeException());
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).salvarCliente(any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Search for a client by ID in the database.")
	public void encontrarClientePorId_sucesso_retorno200() throws Exception {
		when(service.buscarClientePorId(1L)).thenReturn(cliente1);
		
		mvc.perform(get("/encontrarcliente/1")).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).buscarClientePorId(1L);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Searches for a client that doesn't exist, returns 404")
	public void encontrarClientePorId_notFound_retorno404() throws Exception {
		when(service.buscarClientePorId(anyLong())).thenThrow(new ClienteNotFoundException());
		
		mvc.perform(get("/encontrarcliente/1")).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente não encontrado."));

		verify(service).buscarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePorId_verboIncorreto_retorno405() throws Exception{
		mvc.perform(delete("/encontrarcliente/1")).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).buscarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePorId_erroDeServidor_retorno500() throws Exception{
		when(service.buscarClientePorId(anyLong())).thenThrow(new RuntimeException());
		
		mvc.perform(get("/encontrarcliente/1")).andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).buscarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Returns 204, deletes an existing client by ID from database.")
	public void deletarClientePorId_sucesso_retorno204() throws Exception {
		mvc.perform(delete("/deletarporid/1")).andExpect(status().isNoContent());
		
		verify(service).deletarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Deletes a client by a non-existing ID and returns 404")
	public void deletarClientePorId_clienteNaoEncontrado_retorno404() throws Exception{
		doThrow(new ClienteNotFoundException()).when(service).deletarClientePorId(anyLong());
		
		mvc.perform(delete("/deletarporid/1")).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente não encontrado."));
		
		verify(service).deletarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void deletarClientePorId_verboIncorreto_retorno405() throws Exception{
		mvc.perform(get("/deletarporid/1")).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "DELETE"));

		verify(service, never()).deletarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void deletarClientePorId_erroServidor_retorno500() throws Exception{
		doThrow(new RuntimeException()).when(service).deletarClientePorId(anyLong());
		
		mvc.perform(delete("/deletarporid/1")).andExpect(status().isInternalServerError());
		
		verify(service).deletarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Updates client according to the DTO object, returns 200.")
	public void atualizarCliente_sucesso_retorno200() throws Exception{
		when(service.atualizarCliente(anyLong(), any(ClienteRequestDTO.class))).thenReturn(cliente1);
		
		mvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isOk())
		.andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.id").value(1L))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Tries to update and don't find client by ID, returns 404.")
	public void atualizarCliente_clienteNaoEncontrado_retorno404() throws Exception{		
		when(service.atualizarCliente(anyLong(), any(ClienteRequestDTO.class)))
		.thenThrow(new ClienteNotFoundException());
		
		mvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente não encontrado."));

		verify(service).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarCliente_verboIncorreto_retorno405() throws Exception{
		mvc.perform(post("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "PUT"));
		
		verify(service, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Tries to update client, but it is not allowed change the CPF. Returns 409.")
	public void atualizarCliente_trocaDeCpf_retorno409() throws Exception{
		when(service.atualizarCliente(anyLong(), any(ClienteRequestDTO.class)))
		.thenThrow(new AlteracaoDeCpfException());
		
		mvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isConflict())
		.andExpect(content().string("Alteração de CPF não permitida."));
		
		verify(service).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarCliente_erroDeServidor_retorno500() throws Exception{
		when(service.atualizarCliente(anyLong(), any(ClienteRequestDTO.class)))
		.thenThrow(new RuntimeException());
		
		mvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError())
		.andExpect(content().string(containsString("Erro")));
		
		verify(service).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Finds client by CPF, returns 200.")	
	public void encontrarClientePorCpf_sucesso_retorno200() throws Exception{
		when(service.encontrarPorCpf("23501206586")).thenReturn(cliente1);
		
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).encontrarPorCpf("23501206586");
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Tries to find client by CPF but fails. Returns 404")
	public void encontrarClientePorCpf_clienteNotFound_retorno404() throws Exception{
		when(service.encontrarPorCpf("23501206586"))
		.thenThrow(new ClienteNotFoundException("23501206586"));
		
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente com o CPF = 23501206586 não encontrado."));
		
		verify(service).encontrarPorCpf("23501206586");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePor_verboIncorreto_retorno405() throws Exception{
		mvc.perform(delete("/clientecpf/23501206586")).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).encontrarPorCpf("23501206586");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePorCpf_erroDeServidor_retorno500() throws Exception{
		when(service.encontrarPorCpf("23501206586"))
		.thenThrow(new RuntimeException());
	
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).encontrarPorCpf("23501206586");
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Returns Page and 200. We gave the parameters to the Page.")
	public void listaPaginada_sucessoComParâmetros_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginada(0, 2)).thenReturn(page);
		
		mvc.perform(get("/paginada?pagina=0&itens=2")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].id").value(1L))
		.andExpect(jsonPath("$.content[1].id").value(2L))
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));

		verify(service).listaPaginada(0, 2);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Returns Page and 200. We didn't give the parameters, we use the defaultValue from the"
	+ "endpoint.")
	public void listaPaginada_sucessoSemParâmetros_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginada(0, 3)).thenReturn(page);

		mvc.perform(get("/paginada")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].id").value(1L))
		.andExpect(jsonPath("$.content[1].id").value(2L))
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).listaPaginada(0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest @CsvSource({"-1 , 2","0 , 0"}) @DisplayName("Invalid parameters, returns 400.")
	public void listaPaginada_parametrosInvalidos_retorno400(int pagina, int itens) throws Exception {
		when(service.listaPaginada(pagina, itens)).thenThrow(new IllegalArgumentException
		("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		mvc.perform(get("/paginada?pagina="+pagina+"&itens="+itens)).andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		verify(service).listaPaginada(pagina, itens);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginada_verboIncorreto_retorno405() throws Exception {
		mvc.perform(post("/paginada")).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow","GET"));
		
		verify(service, never()).listaPaginada(0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginada_erroDeServidor_retorno500() throws Exception {
		when(service.listaPaginada(0, 3)).thenThrow(new RuntimeException());
		
		mvc.perform(get("/paginada")).andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).listaPaginada(0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Returns 200 and paginated list. No Page parameters are provided.")
	public void listaPaginadaOrdenada_sucessoSemParâmetros_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginadaPorOrdenacao(0, 3, "id")).thenReturn(page);
		
		mvc.perform(get("/paginadaordem").param("ordenadoPor", "id")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).listaPaginadaPorOrdenacao(0, 3, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test @DisplayName("Returns 200 and paginated list. Parameters are provided.")
	public void listaPaginadaOrdenada_sucessoComParâmetros_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginadaPorOrdenacao(0, 2, "id")).thenReturn(page);
		
		mvc.perform(get("/paginadaordem?pagina=0&itens=2&ordenadoPor=id")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).listaPaginadaPorOrdenacao(0, 2, "id");
		verifyNoMoreInteractions(service);	
	}
	
	@ParameterizedTest @CsvSource({"-1 , 2","0 , 0"}) 
	@DisplayName("Returns 400, invalid parameters of the page.")
	public void listaPaginadaOrdenada_parametrosInvalidos_retorno400(int pagina, int itens) 
	throws Exception {
		when(service.listaPaginadaPorOrdenacao(pagina, itens, "id")).thenThrow(new 
		IllegalArgumentException("Página não pode ser negativa e itens não pode ser menor que 1."));
		
		mvc.perform(get("/paginadaordem?pagina="+pagina+"&itens="+itens+"&ordenadoPor=id"))
		.andExpect(status().isBadRequest())
		.andExpect(content().string("Página não pode ser negativa e itens não pode ser menor que 1."));
		
		verify(service).listaPaginadaPorOrdenacao(pagina, itens, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginadaOrdenada_verboIncorreto_retorno405() throws Exception{
		mvc.perform(post("/paginadaordem?pagina=1&itens=2&ordenadoPor=id"))
		.andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).listaPaginadaPorOrdenacao(1, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginadaOrdenada_erroDeServidor_retorno500() throws Exception{
		when(service.listaPaginadaPorOrdenacao(1, 2, "id")).thenThrow(new RuntimeException());
		
		mvc.perform(get("/paginadaordem?pagina=1&itens=2&ordenadoPor=id"))
		.andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).listaPaginadaPorOrdenacao(1, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	@DisplayName("Searches for clients using part of their names and returns a paginated list, "
	+ "retuns 200")
	public void buscarPorNomePagina_sucessoComParametro_retorno200() throws Exception {
		ClienteResponseDTO cliente3 = new ClienteResponseDTO();
		cliente3.setId(3L);
		cliente3.setNome("Marcelo");
		cliente3.setCpf("20219064674");
		cliente3.setEmail("marcelo@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente3);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscarPorNome("mar", 0, 2)).thenReturn(page);
		
		mvc.perform(get("/buscapornome?nome=mar&pagina=0&itens=2")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("marcelo@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).buscarPorNome("mar", 0, 2);
		verifyNoMoreInteractions(service);	
	}

	@Test
	@DisplayName("Searches for clients using part of their names and returns a paginated list, "
	+ "page parameters are not provided. Returns 200.")
	public void buscarPorNomePagina_sucessoSemParametro_retorno200() throws Exception {
		/*Sem parâmetro em nome, página e itens.*/
		ClienteResponseDTO cliente3 = new ClienteResponseDTO();
		cliente3.setId(3L);
		cliente3.setNome("Marcelo");
		cliente3.setCpf("20219064674");
		cliente3.setEmail("marcelo@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente3);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscarPorNome(null, 0, 3)).thenReturn(page);
		
		mvc.perform(get("/buscapornome")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("marcelo@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).buscarPorNome(null, 0, 3);
		verifyNoMoreInteractions(service);	
	}
	
	@ParameterizedTest @CsvSource({"mar, -1, 2", "marc, 0, 0", })
	@DisplayName("Returns 400. Invalid parameters of the page (negative page, items less then 1).")
	public void buscarPorNomePagina_paginaItensInvalidos_retorno400(String nome, int pagina, int itens) 
	throws Exception {
		when(service.buscarPorNome(nome, pagina, itens))
		.thenThrow(new IllegalArgumentException("Página não pode ser negativa e itens maior que 0."));
		
		mvc.perform(get("/buscapornome?nome="+nome+"&pagina="+pagina+"&itens="+itens))
		.andExpect(status().isBadRequest())
		.andExpect(content().string("Página não pode ser negativa e itens maior que 0."));
		
		verify(service).buscarPorNome(nome, pagina, itens);
		verifyNoMoreInteractions(service);	
	}

	@ParameterizedTest @NullAndEmptySource @ValueSource(strings = " ")
	@DisplayName("Returns 400. Invalid parameters (invalid name).")
	public void buscarPorNomePagina_nomeInvalido_retorno400(String nome) throws Exception{
		when(service.buscarPorNome(nome, 0, 2)).thenThrow(new 
		IllegalArgumentException("Nome deve ter entre 3 e 60 caracteres, não pode ser nulo ou vazio."));
		
		mvc.perform(get("/buscapornome?pagina=0&itens=2").param("nome", nome))
		.andExpect(status().isBadRequest()).andExpect(content()
		.string("Nome deve ter entre 3 e 60 caracteres, não pode ser nulo ou vazio."));
		
		verify(service).buscarPorNome(nome, 0, 2);
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void buscarPorNomePagina_verboIncorreto_retorno405() throws Exception{	
		mvc.perform(post("/buscapornome?nome=mar&pagina=0&itens=2"))
		.andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).buscarPorNome("mar", 0, 2);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscarPorNomePagina_erroDeServidor_retorno500() throws Exception{	
		when(service.buscarPorNome("mar", 0, 2)).thenThrow(new RuntimeException());
		
		mvc.perform(get("/buscapornome?nome=mar&pagina=0&itens=2"))
		.andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).buscarPorNome("mar", 0, 2);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarParcial_sucesso_retorno200() throws Exception {
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", "Marcus");
		updates.put("email", "marcus@gmail.com");
		
		cliente1.setNome(updates.get("nome").toString());
		cliente1.setCpf("23501206586");
		cliente1.setEmail(updates.get("email").toString());
		
		when(service.atualizarParcial(1L, updates)).thenReturn(cliente1);
		
		mvc.perform(patch("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).atualizarParcial(1L, updates);
		verifyNoMoreInteractions(service);
 	}
	
	@Test
	public void atualizarParcial_presencaDoId_retorno400() throws Exception{
		Map<String, Object> updates = new HashMap<>();
		updates.put("id", 2L);
		
		when(service.atualizarParcial(eq(1L) ,anyMap()))
		.thenThrow(new IllegalArgumentException("O campo id não pode ser alterado."));
		
		mvc.perform(patch("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string("O campo id não pode ser alterado."));
		
		verify(service).atualizarParcial(eq(1L), anyMap());
		verifyNoMoreInteractions(service);
	}
	
	
	@Test
	public void atualizarParcial_presencaDoCpf_retorno409() throws Exception{
		Map<String, Object> updates = new HashMap<>();
		updates.put("cpf", "23501206586");
		
		when(service.atualizarParcial(eq(1L) ,anyMap())).thenThrow(new AlteracaoDeCpfException());
		
		mvc.perform(patch("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isConflict())
		.andExpect(content().string("Alteração de CPF não permitida."));
		
		verify(service).atualizarParcial(eq(1L), anyMap());
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = " ")
	public void atualizarParcial_nomeNuloVazio_retorno400(String nome) throws Exception{
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", nome);
		
		when(service.atualizarParcial(eq(1L) ,anyMap()))
		.thenThrow(new IllegalArgumentException("O nome não pode ser vazio ou nulo."));
		
		mvc.perform(patch("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string("O nome não pode ser vazio ou nulo."));
		
		verify(service).atualizarParcial(eq(1L), anyMap());
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "marcus@marcus@marcus", "marcus.com", "@marcus.com", "marcus@"})
	public void atualizarParcial_emailInvalido_retorno400(String email) throws Exception{
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", email);
		
		when(service.atualizarParcial(eq(1L) ,anyMap()))
		.thenThrow(new IllegalArgumentException("Formato inválido do e-mail."));
		
		mvc.perform(patch("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string(containsString("inválido")));
		
		verify(service).atualizarParcial(eq(1L), anyMap());
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarParcial_verboIncorreto_retorno405() throws Exception{
		Map<String, Object> updates = new HashMap<>();
		mvc.perform(get("/parcial/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "PATCH"));
		
		verify(service, never()).atualizarParcial(eq(1L), anyMap());
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void buscaPorEmail_sucessoPaginaCheia_comParametros_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of(cliente1);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscarPorEmail("marcus@gmail.com", 0, 1)).thenReturn(page);
		
		mvc.perform(get("/buscaemail?email=marcus@gmail.com&pagina=0&itens=1"))
		.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1L))
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content.length()").value(1));
		
		verify(service).buscarPorEmail("marcus@gmail.com", 0, 1);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscaPorEmail_sucessoPaginaCheia_semParametros_retorno200() throws Exception{	
		List<ClienteResponseDTO> lista = List.of(cliente1);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscarPorEmail("marcus@gmail.com", 0, 3)).thenReturn(page);
		
		mvc.perform(get("/buscaemail").param("email", "marcus@gmail.com"))
		.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1L))
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content.length()").value(1));
		
		verify(service).buscarPorEmail("marcus@gmail.com", 0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscaPorEmail_sucessoPaginaVazia_retorno200() throws Exception{
		List<ClienteResponseDTO> lista = List.of();
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscarPorEmail("marcus@gmail.com", 0, 3)).thenReturn(page);
		
		mvc.perform(get("/buscaemail?email=marcus@gmail.com")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content.length()").value(0));
		
		verify(service).buscarPorEmail("marcus@gmail.com", 0, 3);
		verifyNoMoreInteractions(service);	
	}
	
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"marcus@marcus@marcus", "marcus.com", "@marcus.com", "marcus@"})
	public void buscaPorEmail_formatoInvalido_retorno400(String email) throws Exception{
		when(service.buscarPorEmail(email, 0, 3))
		.thenThrow(new IllegalArgumentException("Formato inválido do e-mail."));
		
		mvc.perform(get("/buscaemail").param("email", email)).andExpect(status().isBadRequest())
		.andExpect(content().string(containsString("inválido")));
	
		verify(service).buscarPorEmail(email, 0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@CsvSource({"-1,2", "0,0"})
	public void buscaPorEmail_paginaItensInvalidos_retorno400(int pagina, int itens) throws Exception{
		when(service.buscarPorEmail("marcus@gmail.com", pagina, itens)).thenThrow(new 
		IllegalArgumentException("Página não pode ser negativa e itens não pode ser menor que 1."));
		
		mvc.perform(get("/buscaemail").param("email", "marcus@gmail.com")
		.param("pagina", String.valueOf(pagina)).param("itens", String.valueOf(itens)))
		.andExpect(status().isBadRequest())
		.andExpect(content().string("Página não pode ser negativa e itens não pode ser menor que 1."));
		
		verify(service).buscarPorEmail("marcus@gmail.com", pagina, itens);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscaPorEmail_verboIncorreto_rettorno405() throws Exception{
		mvc.perform(delete("/buscaemail").param("email", "marcus@gmail.com"))
		.andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).buscarPorEmail("marcus@gmail.com", 0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscaPorEmail_erroDeServidor_retorno500() throws Exception {
		when(service.buscarPorEmail("marcus@gmail.com", 0, 3)).thenThrow(new RuntimeException());
		
		mvc.perform(get("/buscaemail").param("email", "marcus@gmail.com"))
		.andExpect(status().isInternalServerError())
		.andExpect(content().string(containsString("Erro")));
		
		verify(service).buscarPorEmail("marcus@gmail.com", 0, 3);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarEmail_sucesso_retorno200() throws Exception{
		when(service.atualizarEmail(1L, "marcus@gmail.com")).thenReturn(cliente1);
		
		mvc.perform(patch("/atualizaremail/1").param("email", "marcus@gmail.com"))
		.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Marcus"));
		
		verify(service).atualizarEmail(1L, "marcus@gmail.com");
		verifyNoMoreInteractions(service);	
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "mar", "mar@mar@", "mar.com"})
	public void atualizarEmail_emailInvalido_retorno400(String email) throws Exception{
		when(service.atualizarEmail(1L, email))
		.thenThrow(new IllegalArgumentException("Formato do e-mail inválido."));
		
		mvc.perform(patch("/atualizaremail/1").param("email", email)).andExpect(status().isBadRequest())
		.andExpect(content().string(containsString("inválido")));
		
		verify(service).atualizarEmail(1L, email);	
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarEmail_naoEncontrado_retorno404() throws Exception{
		when(service.atualizarEmail(1L, "marcus@gmail.com")).thenThrow(new ClienteNotFoundException());
		
		mvc.perform(patch("/atualizaremail/1").param("email", "marcus@gmail.com"))
		.andExpect(status().isNotFound()).andExpect(content().string(containsString("não encontrado")));
		
		verify(service).atualizarEmail(1L, "marcus@gmail.com");	
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarEmail_verboIncorreto_retorno405() throws Exception{
		mvc.perform(delete("/atualizaremail/1").param("email", "marcus@gmail.com"))
		.andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", "PATCH"));
		
		verify(service, never()).atualizarEmail(1L, "marcus@gmail.com");	
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarEmail_emailExistente_retorno409() throws Exception{
		when(service.atualizarEmail(1L, "marcus@gmail.com")).thenThrow(new EmailJaCadastradoException());
		
		mvc.perform(patch("/atualizaremail/1").param("email", "marcus@gmail.com"))
		.andExpect(status().isConflict()).andExpect(content().string(containsString("indisponível")));
		
		verify(service).atualizarEmail(1L, "marcus@gmail.com");	
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarEmail_erroDeServidor_retorno500() throws Exception{
		when(service.atualizarEmail(1L, "marcus@gmail.com")).thenThrow(new RuntimeException());
		
		mvc.perform(patch("/atualizaremail/1").param("email", "marcus@gmail.com"))
		.andExpect(status().isInternalServerError())
		.andExpect(content().string(containsString("Erro")));
		
		verify(service).atualizarEmail(1L, "marcus@gmail.com");	
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscarPorEmailOrdenada_sucessoPageCheia_retorno200() throws Exception {
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id")).thenReturn(page);
		
		mvc.perform(get("/buscarporemail").param("email", "marcus@gmail.com")
		.param("pagina", "0").param("itens", "2").param("ordenadoPor", "id"))
		.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1L))
		.andExpect(jsonPath("$.content[1].id").value(2L))
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"));
		
		verify(service).buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscarPorEmailOrdenada_sucessoPageVazia_retorno200() throws Exception {
		List<ClienteResponseDTO> lista = List.of();
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id")).thenReturn(page);
		
		mvc.perform(get("/buscarporemail").param("email", "marcus@gmail.com")
		.param("pagina", "0").param("itens", "2").param("ordenadoPor", "id"))
		.andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0));
		
		verify(service).buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest(name = "Página negativa e itens menor que 1.")
	@CsvSource({"-1,2", "0,0"})
	public void buscarPorEmailOrdenada_paginaItensInvalidos_retorno400
	(int pagina, int itens) throws Exception{
		when(service.buscaEmailPaginadaOrdenada("marcus@gmail.com", pagina, itens, "id"))
		.thenThrow(new IllegalArgumentException());
		
		mvc.perform(get("/buscarporemail").param("email", "marcus@gmail.com")
		.param("pagina", String.valueOf(pagina)).param("itens", String.valueOf(itens))
		.param("ordenadoPor", "id")).andExpect(status().isBadRequest());
		
		verify(service).buscaEmailPaginadaOrdenada("marcus@gmail.com", pagina, itens, "id");
		verifyNoMoreInteractions(service);
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "marcus@marcus@", "marcus.com"})
	public void buscarPorEmailOrdenada_emailInvalido_retorno400(String email) throws Exception{
		when(service.buscaEmailPaginadaOrdenada(email, 0, 2, "id"))
		.thenThrow(new IllegalArgumentException("Formato inválido do e-mail."));
		
		mvc.perform(get("/buscarporemail").param("email", email)
		.param("pagina", "0").param("itens", "2").param("ordenadoPor", "id"))
		.andExpect(status().isBadRequest()).andExpect(content().string(containsString("inválido")));
		
		verify(service).buscaEmailPaginadaOrdenada(email, 0, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscarPorEmailOrdenada_verboIncorreto_retorno405() throws Exception{
		mvc.perform(delete("/buscarporemail").param("email", "marcus@gmail.com")
		.param("pagina", "0").param("itens", "2").param("ordenadoPor", "id"))
		.andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", "GET"));
		
		verify(service, never()).buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void buscarPorEmailOrdenada_erroDeServidor_retorno500() throws Exception{
		when(service.buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id")).
		thenThrow(new RuntimeException());
		
		mvc.perform(get("/buscarporemail").param("email", "marcus@gmail.com")
		.param("pagina", "0").param("itens", "2").param("ordenadoPor", "id"))
		.andExpect(status().isInternalServerError());
	
		verify(service).buscaEmailPaginadaOrdenada("marcus@gmail.com", 0, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Configuration
	@Import(ClienteController.class)
	static class TestConfig {}
	
}