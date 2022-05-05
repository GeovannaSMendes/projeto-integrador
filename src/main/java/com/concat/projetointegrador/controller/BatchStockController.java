package com.concat.projetointegrador.controller;

import com.concat.projetointegrador.dto.BatchStockFilterDTO;
import com.concat.projetointegrador.dto.BatchStockOrdinationDTO;
import com.concat.projetointegrador.model.InboundOrder;
import com.concat.projetointegrador.service.BatchStockService;
import com.concat.projetointegrador.service.InboundOrderService;
import com.concat.projetointegrador.service.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.List;

@RestController
@RequestMapping("/batchstock")
public class BatchStockController {

    @Autowired
    private BatchStockService batchStockService;

    @Autowired
    private InboundOrderService inboundOrderService;

    @Autowired
    private SectorService sectorService;

    /**
     * filter the batch stocks by the params below
     * @param days int - number of days of the intervel to expire a batch stock
     * @param sectorId Long - sector id
     * @param category String - category of product
     * @param asc Integer - sorting as ascendent/descendent (0: desc/ 1: asc/ default: asc)
     * @return the batch stocks filter by the params above
     * @throws InvalidParameterException
     */
    @GetMapping("/duedate")
    public ResponseEntity<List<BatchStockFilterDTO>> filter(
            @RequestParam int days,
            @RequestParam Long sectorId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer asc
    ) throws InvalidParameterException {
        sectorService.findById(sectorId);
        List<InboundOrder> inboundOrderList = inboundOrderService.findBySectorId(sectorId);
        List<BatchStockFilterDTO> batchStockFilterDTOList = batchStockService
                .filterBatchStocks(inboundOrderList, days, category, asc);
        return ResponseEntity.status(HttpStatus.OK).body(batchStockFilterDTOList);
    }

    /**
     * Filter the best selling products
     * @return the best selling products
     */
    @GetMapping("/stock")
    public ResponseEntity<List<BatchStockOrdinationDTO>> listProductsForQuantity() {
        return ResponseEntity.ok(batchStockService.listProductsForQuantity());
    }
}
