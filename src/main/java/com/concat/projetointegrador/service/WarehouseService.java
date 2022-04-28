package com.concat.projetointegrador.service;

import com.concat.projetointegrador.dto.WarehouseDTO;
import com.concat.projetointegrador.dto.WarehouseQuantityProductDTO;
import com.concat.projetointegrador.dto.WarehouseResponseForQuantityProductsDTO;
import com.concat.projetointegrador.exception.EntityNotFound;
import com.concat.projetointegrador.model.BatchStock;
import com.concat.projetointegrador.model.Warehouse;
import com.concat.projetointegrador.repository.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WarehouseService {
    private WarehouseRepository warehouseRepository;

    public WarehouseDTO findById(Long id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);

        if(warehouse.isEmpty()) {
            throw new RuntimeException("Armazém não encontrado!");
        }

        return WarehouseDTO.convertToWarehouseDTO(warehouse.get());
    }

    public List<WarehouseDTO> findAll() {
        List<Warehouse> listWarehouse = warehouseRepository.findAll();

        if(listWarehouse.isEmpty()) {
            throw new RuntimeException("Não existem armazéns registados!");
        }

        return WarehouseDTO.convertToListWarehouse(listWarehouse);
    }

    public List<WarehouseQuantityProductDTO> findAllProductForWarehouse(List<BatchStock> batchProducts, InboundOrderService inboundOrderService) {
        List<WarehouseQuantityProductDTO> warehouseQuantityProductDTOList = new ArrayList<>();
        List<WarehouseQuantityProductDTO> warehouseQuantityProductDTOs = batchProducts.stream().map(batchStock -> WarehouseQuantityProductDTO.map(
                batchStock.getCurrentQuantity(),
                inboundOrderService.findById(batchStock.getInboundOrder().getId()).getSector().getId()
        )).collect(Collectors.toList());
        Map<Long, Integer> sumQuantityForWarehouse = WarehouseResponseForQuantityProductsDTO.sumQuantityForWarehouse(warehouseQuantityProductDTOs);

        sumQuantityForWarehouse.forEach((k,v) -> {
            warehouseQuantityProductDTOList.add(WarehouseQuantityProductDTO.builder().warehouseCode(k).totalQuantity(v).build());
        });

        return warehouseQuantityProductDTOList;
    }

    @Transactional
    public WarehouseDTO create(Warehouse warehouseModel) {
            Optional<Warehouse> warehouse = warehouseRepository.findByName(warehouseModel.getName());

        if(warehouse.isPresent()){
            throw new RuntimeException("Esse armazém já esta cadastrado!");
        }

        return WarehouseDTO.convertToWarehouseDTO(warehouseRepository.save(warehouseModel));
    }

    public WarehouseDTO update(Warehouse warehouseModel, Long id) {
        Warehouse warehouse = warehouseRepository.findById(id).orElseThrow(() -> new EntityNotFound("Este armazém não existe!"));

        warehouse.setName(warehouseModel.getName());
        warehouse.setRegion(warehouseModel.getRegion());

        warehouseRepository.save(warehouse);

        return WarehouseDTO.convertToWarehouseDTO(warehouseRepository.save(warehouse));
    }

    public void delete(Long id) {
        warehouseRepository.deleteById(id);
    }
}
