package com.sistemaclliente;

import static org.assertj.core.api.Assertions.assertThat;

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
	public void saveAndFlush_sucesso_salvaProduto() {
		Cliente cliente1 = new Cliente();
		cliente1.setNome("Marcus");
		cliente1.setCpf("23501206586");
		cliente1.setEmail("marcus@gmail.com");
		
		repository.saveAndFlush(cliente1);
		
		Optional<Cliente> encontrado1 = repository.findById(cliente1.getId());
		
		assertThat(encontrado1).isPresent();		
		assertThat(encontrado1.get().getNome()).isEqualTo("Marcus");		
		assertThat(encontrado1.get().getId()).isEqualTo(1L);		
		assertThat(encontrado1.get().getCpf()).isEqualTo("23501206586");		
		assertThat(encontrado1.get().getEmail()).isEqualTo("marcus@gmail.com");
		
	}
	
}























