package com.infinitio.aivoiceplatform.master.role.repository.impl;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;
import com.infinitio.aivoiceplatform.master.role.repository.RoleRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@Transactional
public class RoleRepositoryImpl implements RoleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RoleResponse create(CreateRoleRequest request) {

        log.info("Executing Stored Procedure : sp_role_create");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_create");

        /*
            Parameters will be added after
            we create the stored procedure.
         */

        return null;
    }

    @Override
    public RoleResponse update(UpdateRoleRequest request) {

        log.info("Executing Stored Procedure : sp_role_update");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_update");

        return null;
    }

    @Override
    public RoleResponse getByPublicId(String publicId) {

        log.info("Executing Stored Procedure : sp_role_get");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_get");

        return null;
    }

    @Override
    public PageResponse<RoleResponse> getAll(
            Integer page,
            Integer size) {

        log.info("Executing Stored Procedure : sp_role_get_all");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_get_all");

        return null;
    }

    @Override
    public void delete(String publicId) {

        log.info("Executing Stored Procedure : sp_role_delete");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_delete");

    }

    @Override
    public void activate(String publicId) {

        log.info("Executing Stored Procedure : sp_role_activate");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_activate");

    }

    @Override
    public void deactivate(String publicId) {

        log.info("Executing Stored Procedure : sp_role_deactivate");

        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("sp_role_deactivate");

    }

}