package com.carlos.pacientes.services;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carlos.commons.clients.CitaClient;
import com.carlos.commons.dto.PacienteRequest;
import com.carlos.commons.dto.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.EntidadRelacionadaException;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import com.carlos.pacientes.entities.Paciente;
import com.carlos.pacientes.mappers.PacienteMapper;
import com.carlos.pacientes.repositories.PacienteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PacienteServiceImpl implements PacienteService {

	private final PacienteRepository pacienteRepository;
	private final PacienteMapper pacienteMapper;
	private final CitaClient citaClient;

	@Override
	@Transactional(readOnly = true)
	public List<PacienteResponse> listar() {
		log.info("Listado de todos los pacientes activos solicitados");

		return pacienteRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(pacienteMapper::entityToResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PacienteResponse obtenerPorId(Long id) {
		return pacienteMapper.entityToResponse(obtenerPacienteOException(id));
	}
	
	@Override
	@Transactional(readOnly = true)
	public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {
		log.info("Buscando paciente sin estado con id: {}", id);
		return pacienteMapper.entityToResponse(pacienteRepository.findById(id).orElseThrow(() ->
		new RecursoNoEncontradoException("Paciente sin estado no encontrado con id: "+ id)));
	}

	@Override
	public PacienteResponse registrar(PacienteRequest request) {
		log.info("Registrando nuevo paciente: {}", request);

		validarEmailUnico(request.email());
		validarTelefonoUnico(request.telefono());

		Paciente paciente = pacienteMapper.requestToEntity(request);
		Double imc = obtenerIMCPaciente(request.peso(), request.estatura());
		paciente.setImc(imc);
		
		paciente.setEmail(validarEmail(request.email()));
		
		paciente.setNumExpediente(obtenerNoExpediente(request.telefono()));

		pacienteRepository.save(paciente);
		log.info("Nuevo paciente registrado con id: {}", paciente.getId());

		return pacienteMapper.entityToResponse(paciente);
	}

	@Override
	public PacienteResponse actualizar(PacienteRequest request, Long id) {
		Paciente paciente = obtenerPacienteOException(id);
		
		log.info("Actualizando paciente con id: {}", id);

		validarCambiosUnicos(request, id);
		
		boolean telefonoCambio = !paciente.getTelefono().equals(request.telefono());
		
		
		if(telefonoCambio) {
			paciente.setTelefono(request.telefono());
			paciente.setNumExpediente(obtenerNoExpediente(request.telefono()));
		}

		boolean cambioIMC = !paciente.getPeso().equals(request.peso()) 
				|| !paciente.getEstatura().equals(request.estatura());


		if(cambioIMC) {
			paciente.setPeso(request.peso());
			paciente.setEstatura(request.estatura());
			paciente.setImc(obtenerIMCPaciente(request.peso(),request.estatura()));
		}
		
	   // paciente.setImc(obtenerIMCPaciente(paciente.getPeso(), paciente.getEstatura()));
		
		pacienteMapper.updateEntityFromRequest(request, paciente);


		log.info("Paciente con id {} actualizado", id);

		return pacienteMapper.entityToResponse(paciente);
	}

	@Override
	public void eliminar(Long id) {
		// borrado logico
		Paciente paciente = obtenerPacienteOException(id);
		log.info("Eliminando Paciente con id: {}", id);
		
		if(citaClient.pacienteTieneCitasActivas(id)) {
			throw new EntidadRelacionadaException("No se puede eliminar un Paciente porque tiene Citas Activas.");
		}
		paciente.setEstadoRegistro(EstadoRegistro.ELIMINADO);

		log.info("Paciente con id {} ha sido marcado como eliminado", id);

	}

	private Paciente obtenerPacienteOException(Long id) {
		log.info("Buscando Paciente activo con id: {}", id);
		return pacienteRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
				.orElseThrow(() -> new RecursoNoEncontradoException("Paciente activo no encontrado con id: " + id));
	}

	private void validarEmailUnico(String email) {
		log.info("Validando email unico");
		if (pacienteRepository.existsByEmailAndEstadoRegistro(email, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("El correo ya está registrado en un paciente activo");
		}
	}
	
	private void validarTelefonoUnico(String telefono) {
		log.info("Validando telefono unico");
		if (pacienteRepository.existsByTelefonoAndEstadoRegistro(telefono, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("El teléfono ya está registrado en un paciente activo");
		}
	}
	
	private void validarCambiosUnicos(PacienteRequest request, Long id) {
		if (pacienteRepository.existsByEmailAndEstadoRegistroAndIdNot(request.email().toLowerCase(), EstadoRegistro.ACTIVO, id)) {
			throw new IllegalArgumentException("El correo ya está registrado en un paciente activo");
		}
		
		if (pacienteRepository.existsByTelefonoAndEstadoRegistroAndIdNot(request.telefono().toLowerCase(), EstadoRegistro.ACTIVO, id)) {
			throw new IllegalArgumentException("El teléfono ya está registrado en un paciente activo" + request.telefono());
		}
	}

	public Double obtenerIMCPaciente(Double peso, Double estatura) {
		    return peso / (estatura * estatura);
	}

	public String validarEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("El email no puede estar vacío");
		}

		String regex = "^[A-Za-z0-9._%+-]+@(gmail\\.com|outlook\\.com|hotmail\\.com|icloud\\.com|yahoo\\.com)$";

		if (!email.matches(regex)) {
			throw new IllegalArgumentException("El email no tiene un formato válido o dominio permitido");
		}
		return email;
	}
	
	public String obtenerNoExpediente(String telefono) {
		
		    StringBuilder noExpediente = new StringBuilder();
		    for(char c: telefono.toCharArray()) {
		    		noExpediente.append(c).append("X");
		    }
		    
		    return noExpediente.toString();
	}

	
}
