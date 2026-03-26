package com.carlos.citas.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.carlos.citas.dto.CitaRequest;
import com.carlos.citas.dto.CitaResponse;
import com.carlos.citas.services.CitaService;
import com.carlos.commons.controllers.CommonController;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
public class CitaController extends CommonController<CitaRequest, CitaResponse, CitaService> {

	public CitaController(CitaService service) {
		super(service);
	}

	@GetMapping("/id-cita/{id}")
	public ResponseEntity<CitaResponse> obtenerPorIdSinEstado(
			@PathVariable @Positive(message = "El ID debe ser positivio") Long id) {
		return ResponseEntity.ok(service.obtenerCitaPorIdSinEstado(id));
	}

	@PatchMapping("/{idCita}/estado/{idEstado}")
	public ResponseEntity<CitaResponse> cambiarEstado(@PathVariable Long idCita, @PathVariable Long idEstado) {
		return ResponseEntity.ok(service.cambiarEstado(idCita, idEstado));
	}

	@GetMapping("/medicos/{idMedico}/tieneCitasActivas")
	public ResponseEntity<Boolean> medicoTieneCitasActivas(@PathVariable Long idMedico) {
		return ResponseEntity.ok(service.medicoTieneCitasActivas(idMedico));
	}

	@GetMapping("/pacientes/{idPaciente}/tieneCitasActivas")
	public ResponseEntity<Boolean> pacienteTieneCitasActivas(@PathVariable Long idPaciente) {
		return ResponseEntity.ok(service.pacienteTieneCitasActivas(idPaciente));
	}
}
