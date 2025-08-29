package com.sistemaclliente;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.controller.ClienteController;
import com.sistemacliente.exception.ClienteNotFoundException;
import com.sistemacliente.exception.ValidationExceptionHandler;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.service.ClienteService;

@WebMvcTest(controllers = ClienteController.class)
@Import(ValidationExceptionHandler.class)
public class ClienteControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@MockBean
	private ClienteService service;
	
	
	@Test
	public void testarlistarClientes_retornar200() throws Exception {
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
	public void testarlistarClientes_listaVazia_retornar200() throws Exception {
		List<ClienteResponseDTO> lista = List.of();
		when(service.listagemCliente()).thenReturn(lista);
		
		mvc.perform(get("/listarclientes")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty())
		.andExpect(jsonPath("$").isArray()).andExpect(content().json("[]"));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Test
	public void testarlistarClientes_retornar500() throws Exception {
		when(service.listagemCliente()).thenThrow(new RuntimeException());
		
		mvc.perform(get("/listarclientes")).andExpect(status().isInternalServerError())
		.andExpect(content().string("Erro interno no servidor."));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	@Test
	public void testarlistarClientes_clienteNaoEncontrado_retornar404() throws Exception {
		when(service.listagemCliente()).thenThrow(new ClienteNotFoundException());
		
		mvc.perform(get("/listarclientes")).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente não encontrado."));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Configuration
	@Import(ClienteController.class)
	static class TestConfig {}
}



















