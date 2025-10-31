package com.sistemaclliente;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.SistemaClientesJavaApplication;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteResponseDTO;
import com.sistemacliente.repository.ClienteRepository;

import jakarta.transaction.Transactional;

@SpringBootTest(classes = SistemaClientesJavaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClienteControllerIntegrationTest {

	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private ClienteRepository repository;
	
	@Test
	public void contextLoads(){}
	
	@Test
	@Transactional
	@DisplayName("")
	public void listarClientes_listaCheia_retornar200() throws Exception {
		repository.deleteAll();
		
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		Cliente cliente2 = new Cliente();
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
		
		mvc.perform(get("/listarclientes")).andExpect(status().isOk())
		.andExpect(jsonPath("$[0].nome").value("Marcus"))
		.andExpect(jsonPath("$[1].nome").value("Antonio")).andExpect(jsonPath("$.length()").value(2));;
	}
	
}






















