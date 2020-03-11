package org.bmsource.dwh.importer.importer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface MappingPresetRepository extends CrudRepository<MappingPreset, Long> {
}
