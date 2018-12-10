package org.bmsource.dwh.multitenancy.groups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GroupRepository extends JpaRepository<Group, Long> {
}
