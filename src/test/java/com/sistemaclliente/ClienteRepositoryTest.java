package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

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
	
	@Test
	public void contextLoad() {}
	
	@Test
	public void saveAndFlush_sucesso_salvaProdutoDeImediato() {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		/*O banco de dados decide como salvar o id.*/
		repository.saveAndFlush(cliente1);
		
		Optional<Cliente> encontrado1 = repository.findById(cliente1.getId());
		
		assertThat(encontrado1).isPresent();		
		assertThat(encontrado1.get().getNome()).isEqualTo("Marcus");		
		assertThat(encontrado1.get().getId()).isEqualTo(cliente1.getId());		
		assertThat(encontrado1.get().getCpf()).isEqualTo("23501206586");		
		assertThat(encontrado1.get().getEmail()).isEqualTo("marcus@gmail.com");
	}
	
	@Test
	public void save_sucesso_salvaProdutoNoBanco() {
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
	
}























