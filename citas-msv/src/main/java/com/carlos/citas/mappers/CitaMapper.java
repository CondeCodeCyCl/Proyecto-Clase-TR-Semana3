package com.carlos.citas.mappers;
import org.springframework.stereotype.Component;
import com.carlos.citas.dto.CitaResponse;
import com.carlos.citas.dto.CitaRequest;
import com.carlos.citas.entities.Cita;
import com.carlos.citas.enums.EstadoCita;
import com.carlos.commons.dto.DatosMedico;
import com.carlos.commons.dto.DatosPaciente;
import com.carlos.commons.dto.MedicoResponse;
import com.carlos.commons.dto.PacienteResponse;
import com.carlos.commons.enums.EstadoRegistro;
import com.carlos.commons.mappers.CommonMapper;

@Component
public class CitaMapper implements CommonMapper<CitaRequest, CitaResponse, Cita>{

	@Override
	public CitaResponse entityToResponse(Cita entity) {
		if(entity == null) return null;
		return new CitaResponse(
				entity.getId(),
				null,
				null,
				entity.getFechaCita(),
				entity.getSintomas(),
			    entity.getEstadoCita().getDescripcion());
	}
	
	public CitaResponse entityToResponse(Cita entity,PacienteResponse paciente, MedicoResponse medico) {
		if(entity == null) return null;
		return new CitaResponse(
				entity.getId(),
				pacienteResponseToDatosPaciente(paciente),
				medicoResponseToDatosMedico(medico),
				entity.getFechaCita(),
				entity.getSintomas(),
			    entity.getEstadoCita().getDescripcion());
	}
	
	@Override
	public Cita requestToEntity(CitaRequest request) {
		if(request == null) return null;
		return Cita.builder()
				.idPaciente(request.idPaciente())
				.idMedico(request.idMedico())
				.fechaCita(request.fechaCita())
				.sintomas(request.sintomas())
				.estadoCita(EstadoCita.PENDIENTE)
				.estadoRegistro(EstadoRegistro.ACTIVO)
				.build();
	}

	@Override
	public Cita updateEntityFromRequest(CitaRequest request, Cita entity) {
		if(entity == null || request == null) return null;
		entity.setIdPaciente(request.idPaciente());
		entity.setIdMedico(request.idMedico());
		entity.setFechaCita(request.fechaCita());
		entity.setSintomas(request.sintomas());
		return entity;
	}
	
	public Cita updateEntityFromRequest(CitaRequest request, Cita entity, EstadoCita estadoCita) {
		if(entity == null || request == null) return null;
		
		updateEntityFromRequest(request, entity);
		entity.setEstadoCita(estadoCita);
		
		return entity;
	}
	
	private DatosPaciente pacienteResponseToDatosPaciente(PacienteResponse paciente) {
		if(paciente == null) return null;
		
		return new DatosPaciente(
				paciente.nombre(),
				paciente.numExpediente(),
				paciente.edad() + " años",
				paciente.peso() + " kg.",
				paciente.estatura() + " m.",
				paciente.imc() + "",
				paciente.telefono());
				
	}
	
	private DatosMedico medicoResponseToDatosMedico(MedicoResponse medico) {
		if (medico == null) return null;
		
		return new DatosMedico(
				medico.nombre(),
				medico.cedulaProfesional(),
				medico.especialidad());
	}
}
