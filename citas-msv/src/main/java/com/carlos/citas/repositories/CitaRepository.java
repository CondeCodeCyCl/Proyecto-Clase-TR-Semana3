package com.carlos.citas.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.carlos.citas.entities.Cita;
import com.carlos.citas.enums.EstadoCita;
import java.util.List;
import java.util.Optional;
import com.carlos.commons.enums.EstadoRegistro;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long>{
	
	List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);
	
	Optional <Cita> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);
	
	
	boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
		    Long idPaciente, 
		    EstadoRegistro estadoRegistro, 
		    List<EstadoCita> estadosCita
		);
	
	boolean existsByIdPacienteAndEstadoRegistroAndEstadoCitaInAndIdNot(
			Long idPaciente,
			EstadoRegistro estadoRegistro,
			List<EstadoCita> estadoCita, 
			Long id);
	
	boolean existsByIdMedicoAndEstadoRegistroAndEstadoCitaIn(
			Long idMedico, 
			EstadoRegistro estadoRegistro, 
			List<EstadoCita> estadoCita);
}
