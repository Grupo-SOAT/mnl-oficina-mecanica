package br.com.fiap.postech.adapter.input.catalogservice.mapper;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import br.com.fiap.postech.domain.catalogservices.model.NeededSupply;
import br.com.fiap.postech.domain.supply.model.Supply;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CatalogServicesMapper {
    public static CatalogServices fromApiRequest(@NonNull CatalogServiceRequest request) {
        return CatalogServicesEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .supplies(fromApiData(request.getNeededSupplies()).stream().map(item -> (NeededSupplyEntity) item).toList())
                .build();
    }

    public static CatalogServices fromApiData(@NonNull CatalogServiceData data) {
        return CatalogServicesEntity.builder()
                .id(data.getId())
                .name(data.getName())
                .description(data.getDescription())
                .basePrice(data.getBasePrice())
                .supplies(fromApiData(data.getNeededSupplies()).stream().map(item -> (NeededSupplyEntity) item).toList())
                .build();
    }

    public static CatalogServiceData toApiData(@NonNull CatalogServices catalogServices) {
        return new CatalogServiceData()
                .id(catalogServices.getId())
                .name(catalogServices.getName())
                .description(catalogServices.getDescription())
                .basePrice(catalogServices.getBasePrice())
                .neededSupplies(toApiData(catalogServices.getSupplies().stream().map(item -> (NeededSupply) item).toList()));
    }

    public static PaginatedCatalogServiceResponse toPaginatedResponse(@NonNull ScrollPage<CatalogServices> page) {
        final var result = new PaginatedCatalogServiceResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(CatalogServicesMapper.toApiData(item)));

        return result;
    }

    public static List<NeededSupply> fromApiData(@NonNull List<NeededSupplyData> data) {
        List<NeededSupply> needSupplyList = new ArrayList<>();
        for(NeededSupplyData neededSupplyData : data){
            needSupplyList.add(NeededSupplyEntity.builder()
                    .supply((SupplyEntity) to(neededSupplyData))
                    .supplyAmount(neededSupplyData.getQuantity()).build());
        }
        return needSupplyList;
    }

    public static Supply to (NeededSupplyData neededSupplydata){
        return SupplyEntity.builder()
                .id(Long.valueOf(neededSupplydata.getIdSupply())).build();
    }

    public static List<NeededSupplyData> toApiData(@NonNull List<NeededSupply> neededSupplyList) {
        List<NeededSupplyData> needSupplyDataList = new ArrayList<>();
        for(NeededSupply neededSupply : neededSupplyList){
            needSupplyDataList.add(new NeededSupplyData()
                    .quantity(neededSupply.getSupplyAmount())
                    .idSupply(neededSupply.getSupply().getId()));
        }
        return needSupplyDataList;
    }
}
