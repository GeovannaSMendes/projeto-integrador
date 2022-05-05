package com.concat.projetointegrador.dto;

import com.concat.projetointegrador.model.Product;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchStockOrdinationDTO {
    private Integer quantity;
    private ProductDTO product;

    public static BatchStockOrdinationDTO map(Integer quantityCurrent, Integer quantityInitial, Product product) {
        return BatchStockOrdinationDTO.builder().product(ProductDTO.convertToProductDTO(product)).quantity(quantityInitial - quantityCurrent).build();
    }
}
