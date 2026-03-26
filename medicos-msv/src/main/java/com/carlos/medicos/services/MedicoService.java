package com.carlos.medicos.services;
import com.carlos.commons.dto.MedicoRequest;
import com.carlos.commons.dto.MedicoResponse;
import com.carlos.commons.services.CrudService;

public interface MedicoService extends CrudService<MedicoRequest, MedicoResponse> {
	
	MedicoResponse obtenerMedicoPorIdSinEstado(Long id);
	
	MedicoResponse cambiarDisponibilidad(Long idMedico, Long idDisponibilidad);
}
