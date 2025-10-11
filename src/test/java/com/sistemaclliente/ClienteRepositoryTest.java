package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import com.sistemacliente.SistemaClientesJavaApplication;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.repository.ClienteRepository;

@DataJpaTest
@ContextConfiguration(classes = SistemaClientesJavaApplication.class)
public class ClienteRepositoryTest {
	
	private Cliente cliente1;
	private Cliente cliente2;
	
	@Autowired
	private ClienteRepository repository;
	
	@BeforeEach
	public void setup() {
		repository.deleteAll();
		
		cliente1 = clienteNovo("Marcus", "23501206586", "marcus@gmail.com");
		cliente2 = clienteNovo("Antonio", "20219064674", "antonio@gmail.com");
		repository.saveAndFlush(cliente1);
		repository.saveAndFlush(cliente2);
	}
	
	private Cliente clienteNovo(String nome, String cpf, String email) {
		Cliente cliente = new Cliente(); /*id gerado pelo banco.*/
		cliente.setNome(nome);
		cliente.setCpf(cpf);
		cliente.setEmail(email);
		
		return cliente;
	}
	
	@Test
	public void contextLoad() {}
	
	@Test
	public void saveAndFlush_sucesso_salvaProdutoDeImediato() {
		repository.deleteAll();
		
		Cliente clienteGerado = clienteNovo("Vinicius", "54879652365", "vinicius@email.com");
		repository.saveAndFlush(clienteGerado);
		
		Optional<Cliente> clienteEncontrado = repository.findById(clienteGerado.getId());
		
		assertThat(clienteEncontrado).isPresent();
		assertThat(clienteEncontrado.get().getNome()).isEqualTo("Vinicius");
		assertThat(clienteEncontrado.get().getCpf()).isEqualTo("54879652365");
		assertThat(clienteEncontrado.get().getEmail()).isEqualTo("vinicius@email.com");
	}
	
	@Test
	public void save_sucesso_salvaProdutoNoBanco() {
		repository.deleteAll();
		
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
		repository.save(cliente1);
		
		Optional<Cliente> encontrado1 = repository.findById(cliente1.getId());
		
		assertThat(encontrado1).isPresent();		
		assertThat(encontrado1.get().getNome()).isEqualTo("Marcus");		
		assertThat(encontrado1.get().getId()).isEqualTo(cliente1.getId());		
		assertThat(encontrado1.get().getCpf()).isEqualTo("23501206586");		
		assertThat(encontrado1.get().getEmail()).isEqualTo("marcus@gmail.com");
	}
	
	@Test
	public void findAll_listaCheia() {	
		List<Cliente> listaClientes = repository.findAll();
		
		assertThat(listaClientes).isNotNull().isNotEmpty().hasSize(2).extracting(Cliente::getNome)
		.containsExactlyInAnyOrder("Marcus", "Antonio");
				
		assertThat(listaClientes).extracting(Cliente::getCpf)
		.containsExactlyInAnyOrder("23501206586", "20219064674");
		
		assertThat(listaClientes).extracting(Cliente::getEmail)
		.containsExactlyInAnyOrder("marcus@gmail.com", "antonio@gmail.com");
	}
	
	@Test
	public void findAll_listaVazia() {
		repository.deleteAll();
		
		List<Cliente> listaClientes = repository.findAll();
		
		assertThat(listaClientes).isNotNull().isEmpty();
	}
	
	@Test
	public void findByCpf_retornaCliente(){		
		Optional<Cliente> clienteResgatado = repository.findByCpf("23501206586");
		
		assertThat(clienteResgatado).isPresent();
		assertThat(clienteResgatado.get().getNome()).isEqualTo("Marcus");
		assertThat(clienteResgatado.get().getCpf()).isEqualTo("23501206586");
		assertThat(clienteResgatado.get().getEmail()).isEqualTo("marcus@gmail.com");
	}
	
	@Test
	public void findByCpf_naoEncontraCliente_retornaVazia() {
		Optional<Cliente> clienteNaoEncontrado = repository.findByCpf("10101010101");
		
		assertThat(clienteNaoEncontrado).isNotPresent();
	}
	
	@Test
	public void findByEmail_retornaCliente() {
		Optional<Cliente> encontrado = repository.findByEmail("marcus@gmail.com");
	
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getNome()).isEqualTo("Marcus");
		assertThat(encontrado.get().getCpf()).isEqualTo("23501206586");
	}
	

	@Test
	public void findByEmail_clienteNaoEncontrado_retornaVazia() {
		Optional<Cliente> encontrado = repository.findByEmail("jorge@gmail.com");
		
		assertThat(encontrado).isNotPresent();
	}
	
	@Test
	public void findById_retornaCliente() {
		Optional<Cliente> encontrado = repository.findById(cliente1.getId());
		
		assertThat(encontrado).isPresent();
		assertThat(encontrado.get().getNome()).isEqualTo("Marcus");
		assertThat(encontrado.get().getCpf()).isEqualTo("23501206586");
		assertThat(encontrado.get().getEmail()).isEqualTo("marcus@gmail.com");
	}
	
	@Test
	public void findById_naoEncontraCliente_retornoVazio() {
		Optional<Cliente> encontrado = repository.findById(99L);
		
		assertThat(encontrado).isNotPresent();
	}
	

	@Test
	public void delete_deletaCliente() {
		repository.delete(cliente1);
		Optional<Cliente> encontrado = repository.findByEmail("marcus@gmail.com");
		
		assertThat(encontrado).isNotPresent();
	}
	
	@Test
	public void findAll_page_retornaPageCheia() {
		PageRequest pageable = PageRequest.of(0, 2);
		Page<Cliente> page = repository.findAll(pageable);
		
		assertThat(page).isNotNull().isNotEmpty().hasSize(2).extracting(Cliente::getNome)
		.containsExactlyInAnyOrder("Marcus", "Antonio");
		
		assertThat(page.getContent()).extracting(Cliente::getCpf)
		.containsExactlyInAnyOrder("20219064674", "23501206586");

		assertThat(page.getContent()).extracting(Cliente::getEmail)
		.containsExactlyInAnyOrder("marcus@gmail.com", "antonio@gmail.com");
	}
	
	@Test
	public void findAll_page_retornaPageVazia() {
		repository.deleteAll();
		
		PageRequest pageable = PageRequest.of(0, 2);
		Page<Cliente> page = repository.findAll(pageable);
		
		assertThat(page).isNotNull().isEmpty();;
		assertThat(page.getTotalPages()).isEqualTo(0);
	}
	
	@Test
	public void findAll_pageOrdenada_retorna_pageCheia() {
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		Page<Cliente> page = repository.findAll(pageable);
		
		assertThat(page).isNotNull().isNotEmpty();
		assertThat(page.getNumberOfElements()).isEqualTo(2);
		/*Aqui vemos que o cliente2, mesmo sendo salvo em segundo no banco está em primeiro na Page por
		 *conta do critério de ordenação.*/
		assertThat(page.getContent().get(0).getNome()).isEqualTo("Antonio");
		assertThat(page.getContent().get(1).getNome()).isEqualTo("Marcus");
	}
	
	@Test
	public void findAll_pageOrdenada_retorna_pageVazia() {
		repository.deleteAll();
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		Page<Cliente> page = repository.findAll(pageable);

		assertThat(page).isNotNull().isEmpty();
	}
	
	@Test
	 public void findByNomeContainingIgnoreCase_sucesso_retornaPageCheia() {
		PageRequest pageable = PageRequest.of(0, 2, Sort.by("nome").ascending());
		Page<Cliente> page = repository.findByNomeContainingIgnoreCase("Marcus", pageable);
		
		assertThat(page).isNotNull().isNotEmpty().hasSize(1).extracting(Cliente::getNome)
		.containsExactly("Marcus");
		assertThat(page.getContent().get(0).getEmail()).isEqualTo("marcus@gmail.com");
		assertThat(page.getContent().get(0).getCpf()).isEqualTo("23501206586");
	}
	
}























