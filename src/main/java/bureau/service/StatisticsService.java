package bureau.service;

import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.repository.ClientRepository;
import bureau.repository.TranslationOrderRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {
    private final ClientRepository clientRepository;
    private final TranslationOrderRepository orderRepository;

    public StatisticsService(ClientRepository clientRepository, TranslationOrderRepository orderRepository) {
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> statistics = new LinkedHashMap<>();
        statistics.put("Всего клиентов", (long) clientRepository.findAll().size());
        List<TranslationOrder> orders = orderRepository.findAll();
        statistics.put("Всего заказов", (long) orders.size());
        statistics.put("Новых", countByStatus(orders, OrderStatus.NEW));
        statistics.put("В работе", countByStatus(orders, OrderStatus.IN_PROGRESS));
        statistics.put("Завершённых", countByStatus(orders, OrderStatus.COMPLETED));
        statistics.put("Отменённых", countByStatus(orders, OrderStatus.CANCELLED));
        return statistics;
    }

    private long countByStatus(List<TranslationOrder> orders, OrderStatus status) {
        return orders.stream().filter(order -> order.getStatus() == status).count();
    }
}
