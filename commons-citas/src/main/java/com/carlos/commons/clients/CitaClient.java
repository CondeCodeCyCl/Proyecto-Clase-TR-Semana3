package com.carlos.commons.clients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.carlos.commons.configuration.FeighClientConfig;

@FeignClient(name = "citas-msv", configuration = FeighClientConfig.class)
public interface CitaClient {

    @GetMapping("/medicos/{idMedico}/tieneCitasActivas")
    boolean medicoTieneCitasActivas(@PathVariable Long idMedico);

    @GetMapping("/pacientes/{idPaciente}/tieneCitasActivas")
    boolean pacienteTieneCitasActivas(@PathVariable Long idPaciente);
}
