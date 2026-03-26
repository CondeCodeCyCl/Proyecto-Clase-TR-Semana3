package com.carlos.citas.services;
import com.carlos.citas.dto.CitaRequest;
import com.carlos.citas.dto.CitaResponse;
import com.carlos.commons.services.CrudService;

public interface CitaService extends CrudService<CitaRequest, CitaResponse>{
	CitaResponse obtenerCitaPorIdSinEstado(Long id);
	
	CitaResponse cambiarEstado(Long idCita, Long idEstado);
	
	boolean medicoTieneCitasActivas(Long idMedico);
	boolean pacienteTieneCitasActivas(Long idPaciente);
}