package com.carlos.medicos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carlos.medicos.entities.Medico;
import java.util.List;
import java.util.Optional;

import com.carlos.commons.enums.EstadoRegistro;


public interface MedicoRepository extends JpaRepository<Medico, Long> {
	
	List<Medico> findByEstadoRegistro(EstadoRegistro estadoRegistro);
	Optional <Medico> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);
	
	boolean existsByTelefonoAndEstadoRegistro(String telefono, EstadoRegistro estadoRegistro);
	boolean existsByEmailAndEstadoRegistro(String email, EstadoRegistro estadoRegistro);
	
	boolean existsByCedulaProfesionalAndEstadoRegistro(String cedulaProfesional, EstadoRegistro estadoRegistro);
	
	boolean existsByTelefonoAndEstadoRegistroAndIdNot (String telefono, EstadoRegistro estadoRegistro, Long id);
	boolean existsByEmailAndEstadoRegistroAndIdNot(String email, EstadoRegistro estadoRegistro, Long id);

	boolean existsByCedulaProfesionalAndIdNotAndEstadoRegistro(String cedulaProfesional, Long id, EstadoRegistro estadoRegistro);
}
