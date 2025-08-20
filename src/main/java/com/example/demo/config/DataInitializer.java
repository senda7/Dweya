package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        createRoleIfNotExists("admin");
        createRoleIfNotExists("utilisateur");
        createRoleIfNotExists("pharmacie");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByNom(roleName) == null) {
            Role role = new Role();
            role.setNom(roleName);
            roleRepository.save(role);
        }
    }
}
