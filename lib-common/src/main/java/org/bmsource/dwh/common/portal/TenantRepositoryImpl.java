//package org.bmsource.dwh.common.portal;
//
//import org.bmsource.dwh.common.utils.TestUtils;
//import org.springframework.stereotype.Repository;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class TenantRepositoryImpl implements TenantRepository {
//
//    public List<Tenant> tenants = new ArrayList<Tenant>() {{
//        add(new Tenant(TestUtils.TENANT1, TestUtils.TENANT1));
//        add(new Tenant(TestUtils.TENANT2, TestUtils.TENANT2));
//    }};
//
//    @Override
//    public List<Tenant> findAll() {
//        return tenants;
//    }
//
//    @Override
//    public Optional<Tenant> findById(String id) {
//        if (id.equals(TestUtils.TENANT1))
//            return Optional.of(tenants.get(0));
//        if (id.equals(TestUtils.TENANT2))
//            return Optional.of(tenants.get(1));
//        return Optional.empty();
//    }
//}
