package com.concat.projetointegrador.unit;

import com.concat.projetointegrador.dto.BatchStockFilterDTO;
import com.concat.projetointegrador.dto.BatchStockOrdinationDTO;
import com.concat.projetointegrador.dto.ProductDTO;
import com.concat.projetointegrador.exception.EntityNotFound;
import com.concat.projetointegrador.model.*;
import com.concat.projetointegrador.repository.BatchStockRepository;
import com.concat.projetointegrador.service.BatchStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class BatchStockServiceTest {

    private BatchStockService service;
    private final BatchStockRepository batchStockRepositoryMock = Mockito.mock(BatchStockRepository.class);

    @BeforeEach
    public void init() {
        service = new BatchStockService(batchStockRepositoryMock);
    }

    private Product mockProduct(){
        return Product.builder()
                .id(1L)
                .price(BigDecimal.valueOf(10))
                .category(Category.CONGELADOS)
                .name("frango")
                .volume(1)
                .seller(Seller.builder().id(1L).name("Jonas").lastName("Viana").build())
                .build();
    }

    private BatchStock mockBatchStock() {
        return BatchStock.builder()
                .category(Category.CONGELADOS)
                .currentQuantity(10)
                .initialQuantity(10)
                .currentTemperature(9)
                .initialTemperature(8)
                .manufacturingDate(LocalDate.now())
                .manufacturingTime(LocalTime.now())
                .dueDate(LocalDate.of(2023, 10, 10))
                .product(mockProduct())
                .build();
    }

    private List<BatchStock> mockListBatchStock() {
        return Arrays.asList(
                BatchStock.builder()
                        .id(0L)
                        .category(Category.CONGELADOS)
                        .currentQuantity(5)
                        .initialQuantity(10)
                        .currentTemperature(9)
                        .initialTemperature(8)
                        .manufacturingDate(LocalDate.now())
                        .manufacturingTime(LocalTime.now())
                        .dueDate(LocalDate.of(2023, 10, 10))
                        .product(mockProduct())
                        .build(),
                BatchStock.builder()
                        .id(1L)
                        .category(Category.CONGELADOS)
                        .currentQuantity(5)
                        .initialQuantity(5)
                        .currentTemperature(9)
                        .initialTemperature(8)
                        .manufacturingDate(LocalDate.now())
                        .manufacturingTime(LocalTime.now())
                        .dueDate(LocalDate.of(2023, 11, 10))
                        .product(mockProduct())
                        .build());
    }

    private List<InboundOrder> mockListInboundOrder(){
        Supervisor supervisor = Supervisor.builder().name("Maria").lastName("Ferraz").build();
        Warehouse werehouse = Warehouse.builder().name("Casa Verder").region("São Paulo").build();
        Sector sector = Sector.builder().id(0L).warehouse(werehouse).supervisor(supervisor).category(Category.CONGELADOS).capacity(100).build();
        return Arrays.asList(
                InboundOrder.builder()
                .id(0L)
                .sector(sector)
                .batchStock(mockListBatchStock())
                .build()
        );
    }

    @Test
    void shouldReturnGetBatchStockById() {
        BatchStock batchStock;
        Mockito.when(batchStockRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.of(mockBatchStock()));
        batchStock = service.findById(Mockito.anyLong());
        assertEquals("frango", batchStock.getProduct().getName());
    }

    @Test
    void shouldReturnGetBatchStockByIdNotFound() {
        Mockito.when(batchStockRepositoryMock.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        Throwable exception = assertThrows(EntityNotFound.class, () -> service.findById(Mockito.anyLong()));
        assertEquals("O estoque não foi encontrado!!", exception.getMessage());
    }

    @Test
    void shouldCreateBatchStock() {
        BatchStock batchStock;
        Mockito.when(batchStockRepositoryMock.save(Mockito.any())).thenReturn(mockBatchStock());
        batchStock = service.create(mockBatchStock());
        assertEquals("frango", batchStock.getProduct().getName());
    }

    @Test
    void shouldReturnProductByBatchStock() {
        List<BatchStock> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        batchStocks = service.findByProductId(Mockito.anyLong(), 1);
        assertEquals("frango", batchStocks.get(0).getProduct().getName());
    }

    @Test
    void shouldReturnProductByBatchStockNotFound() {
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Throwable exception = assertThrows(EntityNotFound.class, () -> service.findByProductId(Mockito.anyLong(), 1));
        assertEquals("Este produto não existe", exception.getMessage());
    }

    @Test
    void shouldReturnProductByBatchStockInsufficientAmount() {
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        Throwable exception = assertThrows(EntityNotFound.class, () -> service.findByProductId(Mockito.anyLong(), 20));
        assertEquals("Estoque insuficiente", exception.getMessage());
    }

    @Test
    void shouldReturnBatchStockOrderById() {
        List<BatchStock> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        batchStocks = service.findAllByProductId(Mockito.anyLong(), "L");
        assertEquals(0L, batchStocks.get(0).getId());
    }

    @Test
    void shouldReturnBatchStockOrderByQuantity() {
        List<BatchStock> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        batchStocks = service.findAllByProductId(Mockito.anyLong(), "C");
        assertEquals(5
                , batchStocks.get(0).getCurrentQuantity());
    }

    @Test
    void shouldReturnBatchStockOrderByDueDate() {
        List<BatchStock> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        batchStocks = service.findAllByProductId(Mockito.anyLong(), "F");
        assertEquals(LocalDate.of(2023, 10, 10), batchStocks.get(0).getDueDate());
    }

    @Test
    void shouldReturnBatchStock() {
        List<BatchStock> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(mockListBatchStock());
        batchStocks = service.findAllByProductId(Mockito.anyLong(), null);
        assertEquals(0L, batchStocks.get(0).getId());
    }

    @Test
    void shouldReturnBatchStockNotFound() {
        Mockito.when(batchStockRepositoryMock.findAllByProductId(Mockito.anyLong())).thenReturn(new ArrayList<>());
        Throwable exception = assertThrows(EntityNotFound.class, () -> service.findAllByProductId(Mockito.anyLong(), null));
        assertEquals("Não foi encontrado nenhum lote para esse produto!", exception.getMessage());
    }

    @Test
    void shouldFilterBatchStocksParameterAsc() {
        List<BatchStockFilterDTO> batchStocks;
        batchStocks = service.filterBatchStocks(mockListInboundOrder(), 800, "congelados", 0);
        assertEquals(LocalDate.of(2023,11,10), batchStocks.get(0).getDueDate());
    }

    @Test
    void shouldFilterBatchStocks() {
        List<BatchStockFilterDTO> batchStocks;
        batchStocks = service.filterBatchStocks(mockListInboundOrder(), 800, "congelados", null);
        assertEquals(LocalDate.of(2023,10,10), batchStocks.get(0).getDueDate());
    }

    @Test
    void shouldFilterBatchStocksInvalidParameter() {
        Throwable exception = assertThrows(InvalidParameterException.class, () -> service.filterBatchStocks(mockListInboundOrder(), 800, "congelados", 9));
        assertEquals("O valor de asc deve ser 0 ou 1!", exception.getMessage());
    }

    @Test
    void shouldReturnTheBestSellingProducts() {
        List<BatchStockOrdinationDTO> batchStocks;
        Mockito.when(batchStockRepositoryMock.findAll()).thenReturn(mockListBatchStock());
        batchStocks = service.listProductsForQuantity();
        assertEquals(0, batchStocks.get(0).getQuantity());
        assertEquals(mockProduct().getName(), batchStocks.get(0).getProduct().getName());
        assertEquals(5, batchStocks.get(1).getQuantity());
        assertEquals(mockProduct().getName(), batchStocks.get(1).getProduct().getName());
    }

    @Test
    void shouldReturnBatchStockEntityNotFound() {
        Mockito.when(batchStockRepositoryMock.findAll()).thenReturn(new ArrayList<>());
        Throwable exception = assertThrows(EntityNotFound.class, () -> service.listProductsForQuantity());
        assertEquals("Não existem batchstock registados!", exception.getMessage());
    }

}
