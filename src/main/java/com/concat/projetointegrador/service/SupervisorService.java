package com.concat.projetointegrador.service;

import com.concat.projetointegrador.DTO.SupervisorDto;
import com.concat.projetointegrador.model.SupervisorModel;
import com.concat.projetointegrador.repository.SupervisorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupervisorService {
    private SupervisorRepository supervisorRepository;

    public SupervisorService(SupervisorRepository supervisorRepository) {
        super();
        this.supervisorRepository = supervisorRepository;
    }

    public SupervisorModel create(SupervisorModel supervisor) {
        if (supervisor.getName().length() < 3) throw new RuntimeException();
        return supervisorRepository.save(supervisor);
    }

    public List<SupervisorModel> list() {
        return supervisorRepository.findAll();
    }

    public SupervisorModel getSupervisor(Long id) {
        return supervisorRepository.findById(id).orElse(new SupervisorModel());
    }

    public SupervisorModel update(Long id, SupervisorDto supervisor) {
        SupervisorModel supervisorModel = supervisorRepository.findById(id).orElse(new SupervisorModel());
        supervisorModel.setName(supervisor.getName());
        supervisorModel.setLastname(supervisor.getLastname());
        return supervisorRepository.save(supervisorModel);
    }
}