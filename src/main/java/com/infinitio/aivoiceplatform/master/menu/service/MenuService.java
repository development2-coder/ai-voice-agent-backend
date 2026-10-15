package com.infinitio.aivoiceplatform.master.menu.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.menu.dto.request.CreateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.request.UpdateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import java.util.List;

public interface MenuService {

    MenuResponse create(CreateMenuRequest request);

    MenuResponse update(UpdateMenuRequest request);

    MenuResponse getByPublicId(String publicId);

    List<MenuResponse> getAll();

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}