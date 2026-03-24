package com.carlos.medicos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.carlos.medicos.entities.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

}
