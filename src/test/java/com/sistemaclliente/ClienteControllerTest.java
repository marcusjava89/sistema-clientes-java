package com.sistemaclliente;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
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
import com.sistemacliente.exception.ValidationExceptionHandler;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.service.ClienteService;

@WebMvcTest(controllers = ClienteController.class)
@Import(ValidationExceptionHandler.class)
public class ClienteControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@MockitoBean
	private ClienteService service;
	
	@Test
	public void listar_listaCheia_retornar200() throws Exception {
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
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
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarClientes_verboHttpIncorreto_retorno405() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(get("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "POST"));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_sucesso_retorno200() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
		when(service.salvarCliente(any(ClienteRequestDTO.class))).thenReturn(cliente1);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).salvarCliente(any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_nomeVazio_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_nomeNulo_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome(null);
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_nomeMenor3Digitos_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("ma");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.nome").value("Nome deve ter entre 3 e 60 caracteres"));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_nomeMaior60Digitos_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890AB");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.nome").value("Nome deve ter entre 3 e 60 caracteres"));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_emailVazio_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.email").value("E-mail não pode ser vazio."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_emailInvalido_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.email").value("Formato inválido do e-mail."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_emailNulo_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail(null);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.email").value("E-mail não pode ser vazio."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfInvalido_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("101089757er");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value("Digite os 11 dígitos do CPF sem ponto e hífen."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfMenosDe11Digitos_retorno400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("101089757");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value("Digite os 11 dígitos do CPF sem ponto e hífen."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfMaisDe11Digitos_retornar400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("25013569874965");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value("Digite os 11 dígitos do CPF sem ponto e hífen."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfVazio_retornar400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfNulo_retornar400() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf(null);
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value("CPF não pode ser vazio."));
		
		verify(service, never()).salvarCliente(dto);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void salvarCliente_cpfExistente_retornar409() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
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
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		when(service.salvarCliente(any(ClienteRequestDTO.class))).thenThrow(new RuntimeException());
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto)))
		.andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).salvarCliente(any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePorId_sucesso_retorno200() throws Exception {
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
		when(service.buscarClientePorId(1L)).thenReturn(cliente1);
		
		mvc.perform(get("/encontrarcliente/1")).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).buscarClientePorId(1L);
		verifyNoMoreInteractions(service);
	}
	
	@Test
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
	public void deletarClientePorId_sucesso_retorno204() throws Exception {
		mvc.perform(delete("/deletarporid/1")).andExpect(status().isNoContent());
		
		verify(service).deletarClientePorId(anyLong());
		verifyNoMoreInteractions(service);
	}
	
	@Test
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
		.andExpect(header().string("Allow", containsString("DELETE")));

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
	public void atualizarCliente_sucesso_retorno200() throws Exception{
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
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
	public void atualizarCliente_clienteNaoEncontrado_retorno404() throws Exception{
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
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
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "PUT"));
		
		verify(service, never()).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void atualizarCliente_trocaDeCpf_retorno409() throws Exception{
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("26359874014");
		dto.setEmail("marcus@gmail.com");
		
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
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		when(service.atualizarCliente(anyLong(), any(ClienteRequestDTO.class)))
		.thenThrow(new RuntimeException());
		
		mvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isInternalServerError());
		
		verify(service).atualizarCliente(anyLong(), any(ClienteRequestDTO.class));
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void encontrarClientePorCpf_sucesso_retorno200() throws Exception{
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
		when(service.encontrarPorCpf("23501206586")).thenReturn(cliente1);
		
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.nome").value("Marcus"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
		
		verify(service).encontrarPorCpf("23501206586");
		verifyNoMoreInteractions(service);
	}
	
	@Test
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
	
	@Test
	public void listaPaginada_sucessoComParâmetros_retorno200() throws Exception{
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
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
	public void listaPaginada_sucessoSemParâmetros_retorno200() throws Exception{
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
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
	
	@Test
	public void listaPaginada_paginaNegativa_retorno400() throws Exception{
		when(service.listaPaginada(-1, 2)).thenThrow
		(new IllegalArgumentException("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		mvc.perform(get("/paginada?pagina=-1&itens=2")).andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		verify(service).listaPaginada(-1, 2);
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginada_itensMenorQue1_retorno400() throws Exception{
		when(service.listaPaginada(0, 0)).thenThrow
		(new IllegalArgumentException("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		mvc.perform(get("/paginada?pagina=0&itens=0")).andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
		
		verify(service).listaPaginada(0, 0);
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
	
	@Test
	public void listaPaginadaOrdenada_sucessoSemParâmetros_retorno200() throws Exception{
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginadaPorOrdenacao(0, 3, "nome")).thenReturn(page);
		
		mvc.perform(get("/paginadaordem")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).listaPaginadaPorOrdenacao(0, 3, "nome");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginadaOrdenada_sucessoComParâmetros_retorno200() throws Exception{
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
		Page<ClienteResponseDTO> page = new PageImpl<>(lista);
		
		when(service.listaPaginadaPorOrdenacao(1, 2, "id")).thenReturn(page);
		
		mvc.perform(get("/paginadaordem?pagina=1&itens=2&ordenadoPor=id")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
		
		verify(service).listaPaginadaPorOrdenacao(1, 2, "id");
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void listaPaginadaOrdenada_paginaNegativa_retorno400() throws Exception{
		when(service.listaPaginadaPorOrdenacao(-1, 2, "id"))
		.thenThrow(new IllegalArgumentException("Página não pode ser negativa."));
		
		mvc.perform(get("/paginadaordem?pagina=-1&itens=2&ordenadoPor=id"))
		.andExpect(status().isBadRequest()).andExpect(content().string("Página não pode ser negativa."));
		
		verify(service).listaPaginadaPorOrdenacao(-1, 2, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginadaOrdenada_itensMenorQue1_retorno400() throws Exception{
		when(service.listaPaginadaPorOrdenacao(1, 0, "id"))
		.thenThrow(new IllegalArgumentException("Itens não pode ser menor que 1."));
		
		mvc.perform(get("/paginadaordem?pagina=1&itens=0&ordenadoPor=id"))
		.andExpect(status().isBadRequest()).andExpect(content().string("Itens não pode ser menor que 1."));
		
		verify(service).listaPaginadaPorOrdenacao(1, 0, "id");
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void listaPaginadaOrdenada_verboIncorreto_retorno405() throws Exception{
		
		mvc.perform(post("/paginadaordem?pagina=1&itens=2&ordenadoPor=id"))
		.andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "GET"));
		
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
	public void buscarPorNomePagina_sucessoComParametro_retorno200() throws Exception {
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Marcelo");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("marcelo@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
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
	public void buscarPorNomePagina_sucessoSemParametro_retorno200() throws Exception {
		ClienteResponseDTO cliente1 = new ClienteResponseDTO();
		cliente1.setId(1L);
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		ClienteResponseDTO cliente2 = new ClienteResponseDTO();
		cliente2.setId(2L);
		cliente2.setNome("Marcelo");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("marcelo@gmail.com");
		
		List<ClienteResponseDTO> lista = List.of(cliente1, cliente2);
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
	
	@Test
	public void buscarPorNomePagina_paginaNegativa_retorno400() throws Exception{	
		when(service.buscarPorNome("mar", -1, 2))
		.thenThrow(new IllegalArgumentException("Página não pode ser negativa."));
		
		mvc.perform(get("/buscapornome?nome=mar&pagina=-1&itens=2")).andExpect(status().isBadRequest())
		.andExpect(content().string("Página não pode ser negativa."));
		
		verify(service).buscarPorNome("mar", -1, 2);
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void buscarPorNomePagina_itensMenorQue1_retorno400() throws Exception{	
		when(service.buscarPorNome("mar", 0, 0))
		.thenThrow(new IllegalArgumentException("Itens não pode ser menor que 1."));
		
		mvc.perform(get("/buscapornome?nome=mar&pagina=0&itens=0")).andExpect(status().isBadRequest())
		.andExpect(content().string("Itens não pode ser menor que 1."));
		
		verify(service).buscarPorNome("mar", 0, 0);
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void buscarPorNomePagina_nomeNulo_retorno400() throws Exception{	
		when(service.buscarPorNome(null, 0, 2))
		.thenThrow(new IllegalArgumentException("Nome não pode ser nulo."));
		
		mvc.perform(get("/buscapornome?pagina=0&itens=2")).andExpect(status().isBadRequest())
		.andExpect(content().string("Nome não pode ser nulo."));
		
		verify(service).buscarPorNome(null, 0, 2);
		verifyNoMoreInteractions(service);	
	}
	
	@Test
	public void buscarPorNomePagina_nomeVazio_retorno400() throws Exception{	
		when(service.buscarPorNome("", 0, 2))
		.thenThrow(new IllegalArgumentException("Nome não pode ser vazio."));
		
		mvc.perform(get("/buscapornome?nome=&pagina=0&itens=2")).andExpect(status().isBadRequest())
		.andExpect(content().string("Nome não pode ser vazio."));
		
		verify(service).buscarPorNome("", 0, 2);
		verifyNoMoreInteractions(service);	
	}
	
	@Configuration
	@Import(ClienteController.class)
	static class TestConfig {}
	
}




















