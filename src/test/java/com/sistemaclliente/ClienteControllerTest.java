package com.sistemaclliente;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.controller.ClienteController;
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
		
		mvc.perform(get("/listarclientes").contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1))
		.andExpect(jsonPath("$[1].id").value(2)).andExpect(jsonPath("$[0].nome").value("Marcus"))
		.andExpect(jsonPath("$[1].nome").value("Antonio"));
		
		verify(service).listagemCliente();
		verifyNoMoreInteractions(service);
	}
	
	@Configuration
	@Import(ClienteController.class)
	static class TestConfig {}
}



















