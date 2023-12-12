package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Stock 서비스에서는 부모의 트랜잭션과 별도로 실행이 되어야 하기 때문에 REQUIRES_NEW 사용
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고를 감소한뒤
        // 갱신된 값을 저장
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
}
