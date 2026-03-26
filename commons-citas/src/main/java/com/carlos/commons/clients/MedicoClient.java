package com.carlos.commons.clients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.carlos.commons.configuration.FeighClientConfig;
import com.carlos.commons.dto.MedicoResponse;

@FeignClient(name = "medicos-msv", configuration = FeighClientConfig.class)
public interface MedicoClient {

	@GetMapping("/{id}")
	MedicoResponse obtenerMedicoPorId(@PathVariable Long id);
	
	@GetMapping("/id-medico/{id}")
	MedicoResponse obtenerMedicoPorIdSinEstado(@PathVariable Long id);
	
	@PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
	MedicoResponse cambiarDisponibilidad(
			@PathVariable Long idMedico,
			@PathVariable Long idDisponibilidad);
}