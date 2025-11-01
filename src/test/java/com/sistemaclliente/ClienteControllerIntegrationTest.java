package com.sistemaclliente;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.SistemaClientesJavaApplication;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
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
	@DisplayName("Returns 200 and a list of the clients from the database.")
	public void listarClientes_fullList_return200() throws Exception {
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
		.andExpect(jsonPath("$[1].nome").value("Antonio")).andExpect(jsonPath("$.length()").value(2));
	}
	
	@Test
	@Transactional
	@DisplayName("Returns 200 and an empty list because we clened the database.")
	public void listarClientes_emptyList_return200() throws Exception {
		repository.deleteAll();
		mvc.perform(get("/listarclientes")).andExpect(status().isOk())
		.andExpect(jsonPath("$.length()").value(0));
	}
	
	@Test
	@Transactional
	@DisplayName("Returns 201 when saving DTO client.")
	public void salvarCliente_success_return201() throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").content(mapper.writeValueAsString(dto))
		.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated())
		.andExpect(jsonPath("$.nome").value("Marcus")).andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "ab", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12345678901"})
	@DisplayName("Returns 400, tries to save a client with an invalid name. A name is invalid if it"
	+" is empty, null, have  less then 3 characters or more then 60 characters.")
	public void salvarCliente_invalidName_returns400(String name) throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome(name);
		dto.setCpf("23501206586");
		dto.setEmail("marcus@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.nome")
		.value("Nome deve ter entre 3 e 60 caracteres, não pode ser nulo ou vazio."));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"com", " ", "marcus@marcus@"})
	@DisplayName("Returns 400 when trying to save a client with an invalid email adress.")
	public void salvarCliente_invalidEmail_retorno400(String email) throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail(email);
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.email").value(containsString("inválido")));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {" ", "101089757er", "101089757", "25013569874965"})
	@DisplayName("Returns 400, tries to save a client with an invalid.")
	public void salvarCliente_InvalidCpf_returns400(String cpf) throws Exception {
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf(cpf);
		dto.setEmail("marcus@email.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.cpf").value(containsString("11 dígitos do CPF")));
	}
	
}





















