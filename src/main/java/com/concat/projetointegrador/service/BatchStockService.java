package com.concat.projetointegrador.service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.concat.projetointegrador.dto.BatchStockDTO;
import com.concat.projetointegrador.dto.BatchStockFilterDTO;
import com.concat.projetointegrador.dto.BatchStockOrdinationDTO;
import com.concat.projetointegrador.model.InboundOrder;
import org.springframework.stereotype.Service;
import com.concat.projetointegrador.exception.EntityNotFound;
import com.concat.projetointegrador.model.BatchStock;
import com.concat.projetointegrador.repository.BatchStockRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BatchStockService {

    private BatchStockRepository batchStockRepository;

    /**
     * Search for a batch stock by id
     *
     * @param id Long - batch stock id
     * @return a batch stock if registered
     */
    public BatchStock findById(Long id) {
        Optional<BatchStock> batchStock = batchStockRepository.findById(id);
        if (batchStock.isPresent()) {
            return batchStock.get();
        } else {
            throw new EntityNotFound("O estoque não foi encontrado!!");
        }
    }

    /**
     * Fetches a list of batch stock, filters by validity and sorts if you pass the parameter
     *
     * @param inboundOrderList - inbound order list
     * @param numberOfDays     Integer - number of days to expiration
     * @param category         String - batch stock category
     * @param asc              Integer - sorting type
     * @return a list of Batch Stock Filter DTO
     * @throws InvalidParameterException - returns an exception if the sort parameter is not zero or one
     */
    public List<BatchStockFilterDTO> filterBatchStocks(List<InboundOrder> inboundOrderList, Integer numberOfDays,
                                                       String category, Integer asc) throws InvalidParameterException {
        LocalDate expireTilDate = LocalDate.now().plusDays(numberOfDays);
        List<BatchStockFilterDTO> batchStockFilterDTOList;

        List<BatchStock> batchStockList = inboundOrderList
                .stream()
                .flatMap(inboundOrder -> inboundOrder.getBatchStock().stream())
                .collect(Collectors.toList());

        batchStockFilterDTOList = batchStockList
                .stream()
                .filter(batchStock -> expireTilDate.isAfter(batchStock.getDueDate()))
                .map(BatchStockFilterDTO::convertToDTO)
                .sorted(Comparator.comparing(BatchStockFilterDTO::getDueDate))
                .collect(Collectors.toList());

        if (category != null) {
            batchStockFilterDTOList = batchStockFilterDTOList
                    .stream()
                    .filter(
                            batchStock -> batchStock.getCategory().equalsIgnoreCase(category)
                    )
                    .collect(Collectors.toList());
        }

        if (asc != null && asc == 0) {
            return batchStockFilterDTOList
                    .stream()
                    .sorted((b1, b2) -> b2.getDueDate().compareTo(b1.getDueDate()))
                    .collect(Collectors.toList());
        } else if (asc != null && asc != 1) {
            throw new InvalidParameterException("O valor de asc deve ser 0 ou 1!");
        }
        return batchStockFilterDTOList;
    }

    /**
     * Save a batch stock
     *
     * @param batchStock - batch stock object to insert
     * @return a batch stock
     */
    public BatchStock create(BatchStock batchStock) {
        return batchStockRepository.save(batchStock);
    }

    /**
     * Search a batch stock list by product id and quantity
     *
     * @param id       Long - product id
     * @param quantity Integer - product quantity
     * @return a batch stock list
     */
    public List<BatchStock> findByProductId(Long id, Integer quantity) {
        List<BatchStock> doesTheBatchStockExist = getBatchStocks(id);
        if (doesTheBatchStockExist.isEmpty()) {
            throw new EntityNotFound("Este produto não existe");
        }
        Integer produtcQuantityTotal = doesTheBatchStockExist.stream().reduce(0, (acc, e) -> acc + e.getCurrentQuantity(), Integer::sum);
        if (produtcQuantityTotal < quantity) {
            throw new EntityNotFound("Estoque insuficiente");
        }
        return doesTheBatchStockExist;
    }

    /**
     * Fetches a batch stock list by product id and sorts it from the parameter
     *
     * @param id      Long - product id
     * @param orderBy String - parameter for sorting ("L", "C", "F", null)
     * @return a list of batch stock that can be sorted
     */
    public List<BatchStock> findAllByProductId(Long id, String orderBy) {
        List<BatchStock> batchStocks = getBatchStocks(id);
        if (batchStocks.isEmpty()) {
            throw new EntityNotFound("Não foi encontrado nenhum lote para esse produto!");
        }
        if (orderBy == null) {
            orderBy = "";
        }

        switch (orderBy) {
            case "L":
                return batchStocks
                        .stream()
                        .sorted(Comparator.comparing(BatchStock::getId))
                        .collect(Collectors.toList());
            case "C":
                return batchStocks
                        .stream()
                        .sorted(Comparator.comparing(BatchStock::getCurrentQuantity))
                        .collect(Collectors.toList());
            case "F":
                return batchStocks
                        .stream()
                        .sorted(Comparator.comparing(BatchStock::getDueDate))
                        .collect(Collectors.toList());
        }
        return batchStocks;
    }

    /**
     * fetches a list of batch stock by product id that are at least three weeks before expiry
     *
     * @param id Long - product id
     * @return a list of batch stock within the validity period
     */
    private List<BatchStock> getBatchStocks(Long id) {
        List<BatchStock> batchStocks = batchStockRepository.findAllByProductId(id);
        batchStocks = batchStocks
                .stream()
                .filter(batchStock
                                -> (ChronoUnit.WEEKS.between(
                                LocalDate.now(),
                                batchStock.getDueDate()
                        ) >= 3) && (
                                batchStock.getCurrentQuantity() > 0
                        )
                ).collect(Collectors.toList());
        return batchStocks;
    }

    /**
     * Search for best selling products
     * @return the products in order of the best sellers to the least sold
     */
    public List<BatchStockOrdinationDTO> listProductsForQuantity() {
        List<BatchStock> batchStock = batchStockRepository.findAll();
        if (batchStock.isEmpty()) {
            throw new EntityNotFound("Não existem batchstock registados!");
        }
        List<BatchStockOrdinationDTO> listBatchStockForQuantityDesc = batchStock.stream().map(batchStocks -> (
                BatchStockOrdinationDTO.map(batchStocks.getCurrentQuantity(), batchStocks.getInitialQuantity(), batchStocks.getProduct()))).collect(Collectors.toList());
        listBatchStockForQuantityDesc.sort((i, f) -> i.getQuantity().compareTo(f.getQuantity()));
        return listBatchStockForQuantityDesc;
    }
}
