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

		// validad que existan Paciente y Médico
		PacienteResponse paciente = obtenerPacienteResponse(request.idPaciente());
		MedicoResponse medico = obtenerMedicoResponse(request.idMedico());

		Cita cita = citaRepository.save(citaMapper.requestToEntity(request));

		log.info("Cita registrada existosamente: {}", cita);
		return citaMapper.entityToResponse(cita, paciente, medico);
	}

	@Override
	public CitaResponse actualizar(CitaRequest request, Long id) {
		Cita cita = obtenerCitaOException(id);
		log.info("Actualizando Cita con id: {}", id);

		// validad que existan Paciente y Médico

		PacienteResponse paciente = obtenerPacienteResponse(request.idPaciente());
		MedicoResponse medico = obtenerMedicoResponse(request.idMedico());

		EstadoCita estadoNuevo = EstadoCita.fromCodigo(request.idEstadoCita());

		validarCambioEstadoCita(cita.getEstadoCita(), estadoNuevo);

		citaMapper.updateEntityFromRequest(request, cita, estadoNuevo);
		log.info("Cita registrada existosamente: {}", cita);
		return citaMapper.entityToResponse(cita, paciente, medico);
	}

	@Override
	public void eliminar(Long id) {
		Cita cita = obtenerCitaOException(id);
		log.info("Eliminado Cita con id: {}", id);

		validarEstadoCitaAlEliminar(cita);

		// cambiar disponibilidad del Médico a DISPONIBLE solo sí está en PENDIENTE

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
			if (estadoActual != EstadoCita.FINALIZADA) {
				throw new IllegalArgumentException("Una Cita EN CURSO solo puede cambiar a FINALIZADA");
			}
		}
		case FINALIZADA:
		case CANCELADA: {
			throw new IllegalArgumentException("Una cita FINALIZADA o CANCELADA no puede cambiar de estado");
		}
		}
	}
}
