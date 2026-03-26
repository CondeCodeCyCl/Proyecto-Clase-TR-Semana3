package com.carlos.citas.services;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.carlos.citas.dto.CitaRequest;
import com.carlos.citas.dto.CitaResponse;
import com.carlos.citas.entities.Cita;
import com.carlos.citas.enums.EstadoCita;
import com.carlos.citas.mappers.CitaMapper;
import com.carlos.citas.repositories.CitaRepository;
import com.carlos.commons.clients.MedicoClient;
import com.carlos.commons.clients.PacienteClient;
import com.carlos.commons.dto.MedicoResponse;
import com.carlos.commons.dto.PacienteResponse;
import com.carlos.commons.enums.DisponibilidadMedico;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.exceptions.RecursoNoEncontradoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class CitaServiceImpl implements CitaService {

	private final CitaRepository citaRepository;
	private final CitaMapper citaMapper;
	private final PacienteClient pacienteClient;
	private final MedicoClient medicoClient;

	@Override
	@Transactional(readOnly = true)
	public List<CitaResponse> listar() {
		log.info("Listado de todos las citas activas solicitadas");

		return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(cita -> citaMapper.entityToResponse(cita, obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
						obtenerMedicoResponse(cita.getIdMedico())))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CitaResponse obtenerPorId(Long id) {
		log.info("Obteniendo todas las citas");
		Cita cita = obtenerCitaOException(id);
		return citaMapper.entityToResponse(cita, obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
				obtenerMedicoResponseSinEstado(cita.getIdMedico()));
	}

	@Override
	@Transactional(readOnly = true)
	public CitaResponse obtenerCitaPorIdSinEstado(Long id) {
		log.info("Buscando Cita sin estado con id: {}", id);

		Cita cita = citaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Cita sin estado no encontrada con id: " + id));

		return citaMapper.entityToResponse(cita, obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
				obtenerMedicoResponseSinEstado(cita.getIdMedico()));
	}

	@Override
	public CitaResponse registrar(CitaRequest request) {
		log.info("Registrando nueva Cita: {}", request);

		// Validar que existan Paciente y Médico
		PacienteResponse paciente = obtenerPacienteResponse(request.idPaciente());
		
		MedicoResponse medico = obtenerMedicoResponse(request.idMedico());
		
		validarPacienteSinCitaActiva(request.idPaciente());

		if (!medico.disponibilidad().equalsIgnoreCase(DisponibilidadMedico.DISPONIBLE.getDescripcion())) {
	        throw new IllegalArgumentException("El medico no esta disponible");
	    }

		// 1. Mapear
		Cita cita = citaMapper.requestToEntity(request);

		// 2. Aplicar Escudo INMEDIATAMENTE 
		cita.setEstadoCita(EstadoCita.PENDIENTE);

		// 3. Ocupar médico
		cambiarDisponibilidadMedico(request.idMedico(), DisponibilidadMedico.NO_DISPONIBLE);

		// 4. Guardar (Un solo viaje a la base de datos)
		cita = citaRepository.save(cita);

		log.info("Cita registrada existosamente: {}", cita);
		
		return citaMapper.entityToResponse(cita, paciente, medico);
	}

	@Override
	public CitaResponse actualizar(CitaRequest request, Long id) {
		Cita cita = obtenerCitaOException(id);
		log.info("Actualizando Cita con id: {}", id);

		// validad que existan Paciente y Médico
		
		validarPacienteEnEdicion(request.idPaciente(), id);

		PacienteResponse paciente = obtenerPacienteResponse(request.idPaciente());
		MedicoResponse medicoNuevo = obtenerMedicoResponse(request.idMedico());

		EstadoCita estadoConservado = cita.getEstadoCita();

		
		validarCitaActualizableEnEstado(cita);
		
		//validarCambioEstadoCita(cita.getEstadoCita(), estadoConservado);
		
		// --- LÓGICA DE MÉDICOS (EL CORAZÓN DEL PROBLEMA) ---
	    
	    Long idMedicoAnterior = cita.getIdMedico();
	    Long idMedicoNuevo = request.idMedico();

	    if (!idMedicoAnterior.equals(idMedicoNuevo)) {
	        // A. ¡CRÍTICO! Primero validamos si el nuevo médico está libre
	        if (!medicoNuevo.disponibilidad().equalsIgnoreCase(DisponibilidadMedico.DISPONIBLE.getDescripcion())) {
	            throw new IllegalArgumentException("El nuevo médico seleccionado no está disponible");
	        }
	        
	        // B. Si está libre, AHORA SÍ liberamos al viejo (Llamada HTTP)
	        cambiarDisponibilidadMedico(idMedicoAnterior, DisponibilidadMedico.DISPONIBLE);
	        
	        // C. Ocupamos al nuevo médico según el estado conservado
	        cambiarDisponibilidadMedico(idMedicoNuevo, mapearEstadoCitaADisponibilidad(estadoConservado));
	    }

		citaMapper.updateEntityFromRequest(request, cita);
		
		cita.setEstadoCita(estadoConservado);
		
		log.info("Cita actualizada existosamente: {}", cita);
		return citaMapper.entityToResponse(cita, paciente, medicoNuevo);
	}

	@Override
	public void eliminar(Long id) {
		Cita cita = obtenerCitaOException(id);
		log.info("Eliminando ... Cita con id: {}", id);

		validarEstadoCitaAlEliminar(cita);

		if(cita.getEstadoCita() == EstadoCita.PENDIENTE) {
			cambiarDisponibilidadMedico(cita.getIdMedico(), DisponibilidadMedico.DISPONIBLE);
		}

		cita.setEstadoRegistro(EstadoRegistro.ELIMINADO);
		log.info("Cita con id {} ha sido marcada como eliminada", id);

	}

	private Cita obtenerCitaOException(Long id) {
		log.info("Buscando Cita activa con id: {}", id);

		return citaRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
				.orElseThrow(() -> new RecursoNoEncontradoException("Cita activa no encontrada con id: " + id));
	}

	private void validarEstadoCitaAlEliminar(Cita cita) {
		if (cita.getEstadoCita() == EstadoCita.CONFIRMADA || cita.getEstadoCita() == EstadoCita.EN_CURSO) {
			throw new IllegalStateException("No se puede eliminar una cita " + EstadoCita.CONFIRMADA.getDescripcion()
					+ " o " + EstadoCita.EN_CURSO.getDescripcion());
		}
	}

	private PacienteResponse obtenerPacienteResponse(Long idPaciente) {
		return pacienteClient.obtenerPacientePorId(idPaciente);
	}

	private PacienteResponse obtenerPacienteResponseSinEstado(Long idPaciente) {
		return pacienteClient.obtenerPacientePorIdSinEstado(idPaciente);
	}

	private MedicoResponse obtenerMedicoResponse(Long idMedico) {
		return medicoClient.obtenerMedicoPorId(idMedico);
	}

	private MedicoResponse obtenerMedicoResponseSinEstado(Long idMedico) {
		return medicoClient.obtenerMedicoPorIdSinEstado(idMedico);
	}

	private void validarCambioEstadoCita(EstadoCita estadoActual, EstadoCita estadoNuevo) {
		switch (estadoActual) {
		case PENDIENTE: {
			if (estadoNuevo != EstadoCita.CONFIRMADA && estadoNuevo != EstadoCita.CANCELADA) {
				throw new IllegalArgumentException("Una Cita PENDIENTE solo puede cambiar a CONFIRMADA o CANCELADA");
			}
		}
			break;
		case CONFIRMADA: {
			if (estadoNuevo != EstadoCita.EN_CURSO && estadoNuevo != EstadoCita.CANCELADA) {
				throw new IllegalArgumentException("Una Cita CONFIRMADA solo puede cambiar a EN CURSO  o CANCELADA");
			}
		}
			break;
		case EN_CURSO: {
			if (estadoNuevo != EstadoCita.FINALIZADA) {
				throw new IllegalArgumentException("Una Cita EN CURSO solo puede cambiar a FINALIZADA");
			}
		}
			break;
		case FINALIZADA:
		case CANCELADA: {
			throw new IllegalArgumentException("Una cita FINALIZADA o CANCELADA no puede cambiar de estado");
		}
		}
	}
	
	private void cambiarDisponibilidadMedico(Long idMedico, DisponibilidadMedico disponibilidad) {
	    medicoClient.cambiarDisponibilidad(idMedico, disponibilidad.getCodigo());
	    log.info("Disponibilidad del medico {} cambiada a {}", idMedico, disponibilidad.getDescripcion());
	}

	private DisponibilidadMedico mapearEstadoCitaADisponibilidad(EstadoCita estadoCita) {
		return switch (estadoCita) {
		case PENDIENTE, CONFIRMADA -> DisponibilidadMedico.NO_DISPONIBLE;
		case EN_CURSO -> DisponibilidadMedico.EN_CONSULTA;
		case FINALIZADA, CANCELADA -> DisponibilidadMedico.DISPONIBLE;
		};
	}

	private void sincronizarDisponibilidadMedico(Long idMedico, EstadoCita estadoAnterior, EstadoCita estadoNuevo) {
		DisponibilidadMedico disponibilidadAnterior = mapearEstadoCitaADisponibilidad(estadoAnterior);
		DisponibilidadMedico disponibilidadNueva = mapearEstadoCitaADisponibilidad(estadoNuevo);

		// Solo actualizar si la disponibilidad cambia
		if (disponibilidadAnterior != disponibilidadNueva) {
			medicoClient.cambiarDisponibilidad(idMedico, disponibilidadNueva.getCodigo());
			log.info("Disponibilidad del médico {} actualizada de {} a {}", idMedico, disponibilidadAnterior,
					disponibilidadNueva);
		}
	}
	
	private void validarPacienteSinCitaActiva(Long idPaciente) {
	    boolean tieneCitaActiva = citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaIn(
	            idPaciente, 
	            EstadoRegistro.ACTIVO, 
	            List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO));
	    
	    if (tieneCitaActiva) {
	        throw new IllegalArgumentException(
	            "El paciente ya tiene una cita activa. No puede tener mas de una cita simultanea.");
	    }
	}
	
	private void validarCitaActualizableEnEstado(Cita cita) {
		
	 // La regla estricta del documento:
	    if (cita.getEstadoCita() != EstadoCita.PENDIENTE && cita.getEstadoCita() != EstadoCita.CONFIRMADA) {
	        throw new IllegalArgumentException(
	            "La cita solo puede actualizarse mediante PUT en estados PENDIENTE o CONFIRMADA. Estado actual: " 
	            + cita.getEstadoCita().getDescripcion());
	    }
	}
	
	private void validarPacienteEnEdicion(Long idPaciente, Long idCitaActual) {
	    boolean tieneOtraCita = citaRepository.existsByIdPacienteAndEstadoRegistroAndEstadoCitaInAndIdNot(
	            idPaciente, 
	            EstadoRegistro.ACTIVO, 
	            List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO),
	            idCitaActual); // <--- Aquí ignoramos la cita que estamos editando
	    
	    if (tieneOtraCita) {
	        throw new IllegalArgumentException("El paciente ya tiene otra cita activa.");
	    }
	}

	@Override
	public CitaResponse cambiarEstado(Long idCita, Long idEstado) {
log.info("Cambio de estado solicitado para la cita {} al estado {}", idCita, idEstado);
		
		Cita cita = obtenerCitaOException(idCita);
		EstadoCita estadoNuevo = EstadoCita.fromCodigo(idEstado);
		
		// 1. Validar si la transición es permitida (tu método actual funciona perfecto)
		validarCambioEstadoCita(cita.getEstadoCita(), estadoNuevo);
		
		// 2. Sincronizar al médico (solo es el mismo médico, así que usamos tu método existente)
		sincronizarDisponibilidadMedico(cita.getIdMedico(), cita.getEstadoCita(), estadoNuevo);
		
		// 3. Aplicar el cambio y guardar
		cita.setEstadoCita(estadoNuevo);
		cita = citaRepository.save(cita);
		
		// 4. Retornar la respuesta
		return citaMapper.entityToResponse(
				cita, 
				obtenerPacienteResponse(cita.getIdPaciente()), 
				obtenerMedicoResponse(cita.getIdMedico()));
	}
}
