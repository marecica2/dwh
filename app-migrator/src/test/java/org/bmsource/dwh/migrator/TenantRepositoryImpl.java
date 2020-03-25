package org.bmsource.dwh.migrator;

import org.bmsource.dwh.common.portal.Tenant;
import org.bmsource.dwh.common.portal.TenantRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class TenantRepositoryImpl implements TenantRepository {

    public static Tenant tenant1 = new Tenant();
    public static Tenant tenant2 = new Tenant();
    public static Tenant tenant3 = new Tenant();

    static {
        tenant1.setId("tenant1");
        tenant1.setSchemaName("tenant1");
        tenant2.setId("tenant2");
        tenant2.setSchemaName("tenant2");
        tenant3.setId("tenant3");
        tenant3.setSchemaName("tenant3");
    }

    @Override
    public List<Tenant> findAll() {
        return Arrays.asList(tenant1, tenant2);
    }

    @Override
    public List<Tenant> findAll(Sort sort) {
        return null;
    }

    @Override
    public List<Tenant> findAllById(Iterable<String> strings) {
        return null;
    }

    @Override
    public <S extends Tenant> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Tenant> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Tenant> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Tenant getOne(String s) {
        return null;
    }

    @Override
    public <S extends Tenant> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Tenant> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public Page<Tenant> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Tenant> S save(S s) {
        return null;
    }

    @Override
    public Optional<Tenant> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(Tenant tenant) {

    }

    @Override
    public void deleteAll(Iterable<? extends Tenant> iterable) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Tenant> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Tenant> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Tenant> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Tenant> boolean exists(Example<S> example) {
        return false;
    }
}
