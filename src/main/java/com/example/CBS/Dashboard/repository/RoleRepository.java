package com.example.CBS.Dashboard.repository;

import com.example.CBS.Dashboard.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    Boolean existsByName(String name);

    List<Role> findByNameIn(Collection<String> names);

    List<Role> findAllByOrderByNameAsc();
}
