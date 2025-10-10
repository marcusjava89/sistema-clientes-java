package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import com.sistemacliente.SistemaClientesJavaApplication;
import com.sistemacliente.model.Cliente;
import com.sistemacliente.repository.ClienteRepository;

@DataJpaTest
@ContextConfiguration(classes = SistemaClientesJavaApplication.class)
public class ClienteRepositoryTest {
	
	@Autowired
	private ClienteRepository repository;
	
	@BeforeEach
	public void setup() {
		repository.deleteAll();
		
		Cliente cliente1 = clienteNovo("Marcus", "23501206586", "marcus@gmail.com");
		Cliente cliente2 = clienteNovo("Antonio", "20219064674", "antonio@gmail.com");
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
		
		assertThat(listaClientes).isNotNull();
		assertThat(listaClientes).isNotEmpty();
		assertThat(listaClientes.size()).isEqualTo(2);
		
		assertThat(listaClientes).extracting(Cliente::getNome)
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
		
		assertThat(listaClientes).isNotNull();
		assertThat(listaClientes).isEmpty();
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
		assertThat(clienteNaoEncontrado).isEmpty();
	}
}























