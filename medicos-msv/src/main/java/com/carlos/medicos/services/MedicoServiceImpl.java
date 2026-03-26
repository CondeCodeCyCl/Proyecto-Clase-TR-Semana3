package com.carlos.medicos.services;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.carlos.commons.dto.MedicoRequest;
import com.carlos.commons.dto.MedicoResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EspecialidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.medicos.entities.Medico;
import com.carlos.medicos.mappers.MedicoMapper;
import com.carlos.medicos.repositories.MedicoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class MedicoServiceImpl implements MedicoService{
	
	private final MedicoRepository medicoRepository;
	private final MedicoMapper medicoMapper;

	@Override
	@Transactional(readOnly =true)
	public List<MedicoResponse> listar() {
		log.info("Listado de todos los médicos activos solicitado");
		return medicoRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(medicoMapper::entityToResponse).toList();
	}

	@Override
	@Transactional(readOnly =true)
	public MedicoResponse obtenerPorId(Long id) {
		
		return medicoMapper.entityToResponse(obtenerMedicoOException(id));
	}
	
	@Override
	@Transactional(readOnly =true)
	public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {
		log.info("Buscando medico sin estado con id: {}", id);
		return medicoMapper.entityToResponse(medicoRepository.findById(id).orElseThrow(() ->
		new RecursoNoEncontradoException("Médico sin estado no encontrado con id: "+ id)));
	}

	@Override
	public MedicoResponse registrar(MedicoRequest request) {
		log.info("Registrando nuevo Médico: {}", request.nombre());
		
		validarEmailUnico(request.email());
		validarTelefonoUnico(request.telefono());
		validarCedulaUnico(request.cedulaProfesional());
		
		Medico medico = medicoMapper.requestToEntity(request);
		
		medico.setEspecialidad(EspecialidadMedico.fromCodigo(request.idEspecialidad()));
		medico.setDisponibilidad(DisponibilidadMedico.DISPONIBLE);
		
		medicoRepository.save(medico);
		
		log.info("Médico registrado con éxito: {}", medico.getNombre());
		
		return medicoMapper.entityToResponse(medico);
	}

	@Override
	public MedicoResponse actualizar(MedicoRequest request, Long id) {
		Medico medico = obtenerMedicoOException(id);
		log.info("Actualizando medico con id: {}", id);
		
		validarCambiosUnicos(request, id);
		
		medicoMapper.updateEntityFromRequest(request, medico);
		
		medico.setEspecialidad(EspecialidadMedico.fromCodigo(request.idEspecialidad()));
		
		log.info("Médico actualizado con éxito: {}", id);

		return medicoMapper.entityToResponse(medico);
	}

	@Override
	public void eliminar(Long id) {
		Medico medico = obtenerMedicoOException(id);
		
		// Consultar citas del médico
		
		medico.setEstadoRegistro(EstadoRegistro.ELIMINADO);
		log.info("Médico con id {} ha sido marcado como eliminado", id);
		
	}
	
	private Medico obtenerMedicoOException(Long id) {
		log.info("Buscando Médico activo con id: {}", id);
	
		return medicoRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO).orElseThrow(()
				-> new RecursoNoEncontradoException("Médico activo no encontrado con id: "+ id));
	}
	
	private void validarEmailUnico(String email) {
		log.info("Buscando email unico ...");
	
		if(medicoRepository.existsByEmailAndEstadoRegistro(email.toLowerCase(), EstadoRegistro.ACTIVO)){
			throw new RecursoNoEncontradoException("Ya existe un Médico registrado con el email "+ email);
		}
				
	}
	
	private void validarTelefonoUnico(String telefono) {
		log.info("Buscando telefono unico ...");
	
		if(medicoRepository.existsByTelefonoAndEstadoRegistro(telefono.toLowerCase(), EstadoRegistro.ACTIVO)){
			throw new RecursoNoEncontradoException("Ya existe un Médico registrado con el telefono "+ telefono);
		}		
	}
	
	private void validarCedulaUnico(String cedula) {
		log.info("Buscando cedula unica ...");
	
		if(medicoRepository.existsByCedulaProfesionalAndEstadoRegistro(cedula, EstadoRegistro.ACTIVO)){
			throw new RecursoNoEncontradoException("Ya existe un Médico registrado con la de cédula "+ cedula);
		}		
	}
	
	private void validarCambiosUnicos(MedicoRequest request, Long id) {
		if(medicoRepository.existsByEmailAndEstadoRegistroAndIdNot(request.email().toLowerCase(), EstadoRegistro.ACTIVO, id)) {
			throw new IllegalArgumentException("Ya existe un Paciente registrado con el email: "+ request.email());
		}
		if(medicoRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono().toLowerCase(), EstadoRegistro.ACTIVO, id)) {
			throw new IllegalArgumentException("Ya existe un Paciente registrado con el telefono: "+ request.telefono());
		}
		if(medicoRepository.existsByCedulaProfesionalAndIdNotAndEstadoRegistro(request.cedulaProfesional(), id, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Paciente registrado con la cedula profesional: "+ request.cedulaProfesional());
		}
	}

	@Override
	public MedicoResponse cambiarDisponibilidad(Long idMedico, Long idDisponibilidad) {
		Medico medico = obtenerMedicoOException(idMedico);
	    DisponibilidadMedico nuevaDisponibilidad = DisponibilidadMedico.fromCodigo(idDisponibilidad);
	    medico.setDisponibilidad(nuevaDisponibilidad);
	    log.info("Disponibilidad del médico {} cambiada a {}", idMedico, nuevaDisponibilidad.getDescripcion());
	    return medicoMapper.entityToResponse(medico);
	}
}
