package com.carlos.pacientes.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.carlos.commons.controllers.CommonController;
import com.carlos.commons.dto.PacienteRequest;
import com.carlos.commons.dto.PacienteResponse;
import com.carlos.pacientes.services.PacienteService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@Validated
public class PacienteController extends CommonController<PacienteRequest, PacienteResponse, PacienteService> {
	
	public PacienteController(PacienteService service) {
		super(service);
	}
	
	
	@GetMapping("/id-paciente/{id}")
	public ResponseEntity<PacienteResponse> obtenerPacientePorIdSinEstado(@PathVariable
			@Positive(message = "El ID debe ser positivio") Long id){
		return ResponseEntity.ok(service.obtenerPacientePorIdSinEstado(id));
	}

}
