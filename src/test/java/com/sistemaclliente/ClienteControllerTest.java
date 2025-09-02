package com.sistemaclliente;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.controller.ClienteController;
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
		cliente2.setEmail("marcus@gmail.com");
		
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
		mvc.perform(get("/salvarcliente")).andExpect(status().isMethodNotAllowed())
		.andExpect(header().string("Allow", "POST"));
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
		.andExpect(content().string("O CPF 23501206586 já está cadastrado"));
		
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
	
	@Configuration
	@Import(ClienteController.class)
	static class TestConfig {}
	
}




















