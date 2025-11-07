package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistemacliente.SistemaClientesJavaApplication;
import com.sistemacliente.exception.ValidationExceptionHandler;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.model.dto.ClienteRequestDTO;
import com.sistemacliente.repository.ClienteRepository;

import jakarta.transaction.Transactional;

@SpringBootTest(classes = SistemaClientesJavaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ValidationExceptionHandler.class)
public class ClienteControllerIntegrationTest {

	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private ClienteRepository repository;
	
	@Test
	public void contextLoads(){}
	
	@BeforeEach
	public void setup() {
		repository.deleteAll();
	}
	
	@Test @Transactional @DisplayName("Returns 200 and a list of the clients from the database.")
	public void listarClientes_fullList_return200() throws Exception {
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
	
	@ParameterizedTest @NullAndEmptySource
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
	
	@ParameterizedTest @NullAndEmptySource
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
	
	@Test @Transactional
	@DisplayName("Returns 409, tries to save a client with an existing CPF.")
	public void salvarCliente_existingCPF_returns409() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		ClienteRequestDTO dto1 = new ClienteRequestDTO();
		dto1.setNome("Carlos");
		dto1.setCpf("23501206586");
		dto1.setEmail("carlos@gmail.com");
		
		mvc.perform(post("/salvarcliente").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto1))).andExpect(status().isConflict())
		.andExpect(content().string("O CPF 23501206586 já está cadastrado."));
	}
	
	@Test @Transactional @DisplayName("Search for a client by ID in the database.")
	public void encontrarClientePorId_success_returns200() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		mvc.perform(get("/encontrarcliente/"+cliente1.getId())).andExpect(status().isOk())
		.andExpect(jsonPath("$.nome").value("Marcus")).andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
	}
	
	@Test @DisplayName("Searches for a client that doesn't exist, returns 404")
	public void encontrarClientePorId_notFound_returns404() throws Exception {
		mvc.perform(get("/encontrarcliente/999")).andExpect(status().isNotFound());
	}
	
	@Test @Transactional @DisplayName("Returns 204, deletes an existing client by ID from database.")
	public void deletarClientePorId_success_returns204() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		mvc.perform(delete("/deletarporid/"+cliente1.getId())).andExpect(status().isNoContent());
		
		assertThat(repository.findById(cliente1.getId())).isNotPresent();
	}
	
	@Test @Transactional @DisplayName("Deletes a client by a non-existing ID and returns 404.")
	public void deletarClientePorId_clientNotFound_returns404() throws Exception{
		mvc.perform(delete("/deletarporid/999")).andExpect(status().isNotFound());
		
		assertThat(repository.findById(999L)).isNotPresent();
	}
	
	@Test @Transactional @DisplayName("Updates client according to the DTO object, returns 200.")
	public void atualizarCliente_success_returns200() throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("carlos@gmail.com");
		
		mvc.perform(put("/clientes/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isOk());
		
		Cliente encontrado = repository.findById(cliente1.getId()).get();
		
		assertThat(encontrado.getEmail()).isEqualTo("carlos@gmail.com");
	}
	
	@Test @DisplayName("Tries to update and don't find client by ID, returns 404.")
	public void atualizarCliente_clientNotFound_returns404() throws Exception{
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("23501206586");
		dto.setEmail("carlos@gmail.com");
		
		mvc.perform(put("/clientes/999").contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isNotFound());
	}
	
	@Test @Transactional
	@DisplayName("Tries to update client, but it is not allowed change the CPF. Returns 409.")
	public void atualizarCliente_changeCpf_returns409() throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		ClienteRequestDTO dto = new ClienteRequestDTO();
		dto.setNome("Marcus");
		dto.setCpf("52364587425");
		dto.setEmail("carlos@gmail.com");
		
		mvc.perform(put("/clientes/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(dto))).andExpect(status().isConflict())
		.andExpect(content().string("Alteração de CPF não permitida."));
	}
	
	@Test @Transactional @DisplayName("Finds client by CPF, returns 200.")
	public void encontrarClientePorCpf_success_returns200() throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isOk())
		.andExpect(jsonPath("$.nome").value("Marcus")).andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("marcus@gmail.com"));
	}	
	
	@Test @Transactional @DisplayName("Tries to find client by CPF but fails. Returns 404")
	public void encontrarClientePorCpf_clientNotFound_returns404() throws Exception{
		mvc.perform(get("/clientecpf/23501206586")).andExpect(status().isNotFound())
		.andExpect(content().string("Cliente com o CPF = 23501206586 não encontrado."));
		
		assertThat(repository.findByCpf("23501206586")).isNotPresent();
	}
	
	@Test @Transactional @DisplayName("Returns Page and 200. We gave the parameters to the Page.")
	public void listaPaginada_successWithParameters_returns200() throws Exception{
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
		
		mvc.perform(get("/paginada?pagina=0&itens=2")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@Test @Transactional
	@DisplayName("Returns Page and 200. We didn't give the parameters, we use the defaultValue from the"
	+ "endpoint.")
	public void listaPaginada_successNoParameters_returns200() throws Exception{
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
		
		mvc.perform(get("/paginada")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@ParameterizedTest @CsvSource({"-1 , 2", "0 , 0"}) @DisplayName("Invalid parameters, returns 400.")
	public void listaPaginada_invalidParameters_returns400(int pagina, int itens) throws Exception {
		mvc.perform(get("/paginada?pagina="+pagina+"&itens="+itens)).andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
	}
	
	@Test @Transactional @DisplayName("Returns 200 and paginated list. No Page parameters are provided.")
	public void listaPaginadaOrdenada_successNoParameters_returns200() throws Exception{
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
		
		mvc.perform(get("/paginadaordem").param("ordenadoPor", "nome")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[1].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[1].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[0].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[1].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@Test @Transactional @DisplayName("Returns 200 and paginated list. Parameters are provided.")
	public void listaPaginadaOrdenada_successWithParameters_returns200() throws Exception{
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
		
		mvc.perform(get("/paginadaordem?pagina=0&itens=2&ordenadoPor=id")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[1].nome").value("Antonio"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].cpf").value("20219064674"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].email").value("antonio@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@ParameterizedTest @CsvSource({"-1,2", "0,0"})
	@DisplayName("Returns 400, invalid parameters of the page.")
	public void listaPaginadaOrdenada_invalidParameters_returns400(int pagina, int itens) 
	throws Exception {
		mvc.perform(get("/paginadaordem?pagina="+pagina+"&itens="+itens+"&ordenadoPor=id"))
		.andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
	}
	
	@Test @Transactional 
	@DisplayName("Searches for clients using part of their names and returns a paginated list, "
			+ "parameters are provided. Returns 200.")
	public void buscarPorNomePagina_successWithParameters_returns200() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		Cliente cliente2 = new Cliente();
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		Cliente cliente3 = new Cliente();
		cliente3.setNome("Marcelo");
		cliente3.setCpf("47852136582");
		cliente3.setEmail("marcelo@gmail.com");
		
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
		repository.saveAndFlush(cliente3);
		
		mvc.perform(get("/buscapornome?nome=mar&pagina=0&itens=2")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[1].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[1].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[0].cpf").value("47852136582"))
		.andExpect(jsonPath("$.content[1].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].email").value("marcelo@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@Test @Transactional
	@DisplayName("Searches for clients using part of their names and returns a paginated list, "
	+ "page parameters are not provided. Returns 200.")
	public void buscarPorNomePagina_successNoParameters_returns200() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		Cliente cliente2 = new Cliente();
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		Cliente cliente3 = new Cliente();
		cliente3.setNome("Marcelo");
		cliente3.setCpf("47852136582");
		cliente3.setEmail("marcelo@gmail.com");
		
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
		repository.saveAndFlush(cliente3);
		
		mvc.perform(get("/buscapornome?nome=mar")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[1].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[1].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[0].cpf").value("47852136582"))
		.andExpect(jsonPath("$.content[1].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].email").value("marcelo@gmail.com"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@ParameterizedTest @CsvSource({"mar, -1, 2", "marc, 0, 0", })
	@DisplayName("Returns 400. Invalid parameters of the page (negative page, items less then 1).")
	public void buscarPorNomePagina_invalidParameters_returns400(String nome, int pagina, int itens) 
	throws Exception {
		mvc.perform(get("/buscapornome?nome="+nome+"&pagina="+pagina+"&itens="+itens))
		.andExpect(status().isBadRequest())
		.andExpect(content().string("A página não pode ser negativa e itens não pode ser menor que 1."));
	}
	
	@ParameterizedTest @NullAndEmptySource @ValueSource(strings = " ")
	@DisplayName("Returns 400. Invalid parameters (invalid name).")
	public void buscarPorNomePagina_invalidName_returns400(String nome) throws Exception{
		mvc.perform(get("/buscapornome?pagina=0&itens=2").param("nome", nome))
		.andExpect(status().isBadRequest()).andExpect(content()
		.string("Nome para busca não pode ser vazio ou nulo."));
	}
	
	@Test @Transactional @DisplayName("Returns 200. Partial update of client information.")
	public void atualizarParcial_success_return200() throws Exception {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", "Antonio");
		updates.put("email", "antonio@email.com");
		
		mvc.perform(patch("/parcial/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isOk())
		.andExpect(jsonPath("$.nome").value("Antonio"))
		.andExpect(jsonPath("$.cpf").value("23501206586"))
		.andExpect(jsonPath("$.email").value("antonio@email.com"));
	}
	
	@Test @Transactional @DisplayName("Returns 400 when it tries to update client's ID.")
	public void atualizarParcial_invalidIdUpdanting_returns400() throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("id", 2L);
		
		mvc.perform(patch("/parcial/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string("O campo id não pode ser alterado."));	
	}
	
	@Test @Transactional @DisplayName("Returns 409 when it tries to update client's CPF.")
	public void atualizarParcial_invalidCpfUpdating_returns409() throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("cpf", "58652104789");
		
		mvc.perform(patch("/parcial/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isConflict())
		.andExpect(content().string("Alteração de CPF não permitida."));
	}
	
	@ParameterizedTest @NullAndEmptySource @Transactional @ValueSource(strings = " ") 
	@DisplayName("It tries to update client's name with an empty string and a null value.")
	public void atualizarParcial_invalidName_returns400(String nome) throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("nome", nome);
		
		mvc.perform(patch("/parcial/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string("Nome não pode ser vazio ou nulo."));
	}
	
	@ParameterizedTest @NullAndEmptySource @Transactional
	@ValueSource(strings = {" ", "marcus@marcus@marcus", "marcus.com", "@marcus.com", "marcus@"})
	@DisplayName("It tries to update partial information of the client with an invalid email address and"
	+ "returns 400.")
	public void atualizarParcial_invalidEmailAdress_returns400(String email) throws Exception{
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		repository.saveAndFlush(cliente1);
		
		Map<String, Object> updates = new HashMap<>();
		updates.put("email", email);
		
		mvc.perform(patch("/parcial/"+cliente1.getId()).contentType(MediaType.APPLICATION_JSON)
		.content(mapper.writeValueAsString(updates))).andExpect(status().isBadRequest())
		.andExpect(content().string(containsString("inválido")));
	}
	
	@Test @Transactional() @DisplayName("Searches for an email address and returns a page with the"
	+ "client from that email. Page parameters are provided.")
	public void buscaPorEmail_successFullPage_withPageParameters_returns200() throws Exception{
		/*This is only a test to make sure that the correct content is returned. The e-mail address is 
		 *unique and we can't have two clients with the same e-mail. For that reason the real Page only
		 *return one client.*/
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		Cliente cliente2 = new Cliente();
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		Cliente cliente3 = new Cliente();
		cliente3.setNome("Marcelo");
		cliente3.setCpf("47852136582");
		cliente3.setEmail("marcus@gmail.com");
		
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
		repository.saveAndFlush(cliente3);
		
		mvc.perform(get("/buscaemail?email=marcus@gmail.com&pagina=0&itens=3")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[1].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].cpf").value("47852136582"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@Test @Transactional @DisplayName("Searches for an e-mail address and returns a page with the"
	+ "client from that email. Page parameters are NOT provided.") 
	public void buscaPorEmail_successFullPage_noParameters_returns200() throws Exception{	
		/*This is only a test to make sure that the correct content is returned. The email address is 
		 *unique and we can't have two clients with the same e-mail. For that reason the real Page only
		 *return one client.*/
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");

		Cliente cliente2 = new Cliente();
		cliente2.setNome("Antonio");
		cliente2.setCpf("20219064674");
		cliente2.setEmail("antonio@gmail.com");
		
		Cliente cliente3 = new Cliente();
		cliente3.setNome("Marcelo");
		cliente3.setCpf("47852136582");
		cliente3.setEmail("marcus@gmail.com");
		
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
		repository.saveAndFlush(cliente3);
		
		mvc.perform(get("/buscaemail?email=marcus@gmail.com")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content[0].nome").value("Marcus"))
		.andExpect(jsonPath("$.content[0].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[0].cpf").value("23501206586"))
		.andExpect(jsonPath("$.content[1].nome").value("Marcelo"))
		.andExpect(jsonPath("$.content[1].email").value("marcus@gmail.com"))
		.andExpect(jsonPath("$.content[1].cpf").value("47852136582"))
		.andExpect(jsonPath("$.content.length()").value(2));
	}
	
	@Test @DisplayName("Attempts to search for the client that matches the email and finds none. "
	+ "Returns 200.")
	public void buscaPorEmail_successEmptyPage_returns200() throws Exception{
		mvc.perform(get("/buscaemail?email=marcus@gmail.com")).andExpect(status().isOk())
		.andExpect(jsonPath("$.content.length()").value(0));
	}

}





















