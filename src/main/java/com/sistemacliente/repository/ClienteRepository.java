package com.sistemacliente.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemacliente.model.Cliente;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface ClienteRepository extends JpaRepository<Cliente, Long>{

	public Optional<Cliente> findByCpf(String cpf);
	public Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
	public Page<Cliente> findByEmail(String email, Pageable pageable);
	public Page<Cliente> findByEmailContainingIgnoreCase(String email, Pageable pageable);

}