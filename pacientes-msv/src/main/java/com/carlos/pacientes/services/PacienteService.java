package com.carlos.pacientes.services;

import com.carlos.commons.dto.PacienteRequest;
import com.carlos.commons.dto.PacienteResponse;
import com.carlos.commons.services.CrudService;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse> {

	PacienteResponse obtenerPacientePorIdSinEstado(Long id);
	
}
