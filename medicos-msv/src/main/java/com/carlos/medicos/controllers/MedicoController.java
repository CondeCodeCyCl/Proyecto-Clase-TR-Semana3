package com.carlos.medicos.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carlos.commons.controllers.CommonController;
import com.carlos.commons.dto.MedicoResponse;
import com.carlos.commons.dto.MedicoRequest;
import com.carlos.medicos.services.MedicoService;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
public class MedicoController extends CommonController <MedicoRequest, MedicoResponse, MedicoService> {

	
	public MedicoController (MedicoService service) {
		super(service);
	}
	
	@GetMapping("/id-medico/{id}")
	public ResponseEntity<MedicoResponse> obtenerMedicoPorIdSinEstado(
			@PathVariable
			@Positive(message = "El ID debe ser positivo") Long id){
		
		return ResponseEntity.ok(service.obtenerMedicoPorIdSinEstado(id));
	}
	
	
	//Creamos endpoint para actualizar disponibilidad del medico
	@PutMapping("/{idMedico}/disponibilidad/{idDisponibilidad}")
	public ResponseEntity<MedicoResponse> cambiarDisponibilidad(
	        @PathVariable Long idMedico, 
	        @PathVariable Long idDisponibilidad) {
	    return ResponseEntity.ok(service.cambiarDisponibilidad(idMedico, idDisponibilidad));
	}
}
