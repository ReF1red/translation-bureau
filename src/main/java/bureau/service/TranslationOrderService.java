package bureau.service;

import bureau.exception.BusinessException;
import bureau.exception.EntityNotFoundException;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.repository.TranslationOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TranslationOrderService {
    private final TranslationOrderRepository orderRepository;
    private final ClientService clientService;

    public TranslationOrderService(TranslationOrderRepository orderRepository, ClientService clientService) {
        this.orderRepository = orderRepository;
        this.clientService = clientService;
    }

    public TranslationOrder create(TranslationOrder order) {
        if (order == null) {
            throw new BusinessException("Данные заказа не указаны.");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new BusinessException("Новый заказ должен иметь статус «Новый».");
        }
        order.setId(0);
        order.setCreatedAt(LocalDateTime.now());
        validate(order);
        return orderRepository.create(order);
    }

    public List<TranslationOrder> getAll() {
        return orderRepository.findAll();
    }

    public TranslationOrder getById(long id) {
        if (id <= 0) {
            throw new BusinessException("ID заказа должен быть положительным.");
        }
        TranslationOrder order = orderRepository.findById(id);
        if (order == null) {
            throw new EntityNotFoundException("Заказ с ID " + id + " не найден.");
        }
        return order;
    }

    public void update(TranslationOrder order) {
        if (order == null) {
            throw new BusinessException("Данные заказа не указаны.");
        }
        TranslationOrder previous = getById(order.getId());
        if (previous.getStatus() == OrderStatus.COMPLETED || previous.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Завершённый или отменённый заказ нельзя изменять.");
        }

        order.setCreatedAt(previous.getCreatedAt());
        validate(order);
        if (!order.getDeadline().equals(previous.getDeadline()) && order.getDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException("Новый срок не может быть раньше сегодняшнего дня.");
        }
        validateTransition(previous.getStatus(), order.getStatus());
        if (!orderRepository.update(order)) {
            throw new EntityNotFoundException("Заказ с ID " + order.getId() + " не найден.");
        }
    }

    public void changeStatus(long id, OrderStatus status) {
        TranslationOrder order = getById(id);
        order.setStatus(status);
        update(order);
    }

    public void delete(long id) {
        getById(id);
        if (!orderRepository.delete(id)) {
            throw new EntityNotFoundException("Заказ с ID " + id + " не найден.");
        }
    }

    private void validate(TranslationOrder order) {
        order.setTitle(requiredText(order.getTitle(), "Название", 200));
        order.setSourceLanguage(requiredText(order.getSourceLanguage(), "Исходный язык", 50));
        order.setTargetLanguage(requiredText(order.getTargetLanguage(), "Язык перевода", 50));
        if (order.getSourceLanguage().equalsIgnoreCase(order.getTargetLanguage())) {
            throw new BusinessException("Исходный язык и язык перевода должны различаться.");
        }
        if (order.getDescription() != null) {
            order.setDescription(order.getDescription().trim());
        }
        if (order.getWordCount() <= 0) {
            throw new BusinessException("Количество слов должно быть больше нуля.");
        }

        BigDecimal price = order.getPrice();
        if (price == null || price.signum() < 0 || price.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new BusinessException("Стоимость должна быть от 0 до 9999999999.99 руб.");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("В стоимости допускается не больше двух знаков после запятой.");
        }
        order.setPrice(price.setScale(2));

        if (order.getDeadline() == null || order.getDeadline().isBefore(order.getCreatedAt().toLocalDate())) {
            throw new BusinessException("Срок обязателен и не может быть раньше даты создания заказа.");
        }
        if (order.getStatus() == null) {
            throw new BusinessException("Статус заказа не указан.");
        }
        clientService.getById(order.getClientId());
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return;
        }
        if (from == OrderStatus.NEW && (to == OrderStatus.IN_PROGRESS || to == OrderStatus.CANCELLED)) {
            return;
        }
        if (from == OrderStatus.IN_PROGRESS && (to == OrderStatus.COMPLETED || to == OrderStatus.CANCELLED)) {
            return;
        }
        throw new BusinessException("Нельзя изменить статус «" + from.getTitle() + "» на «" + to.getTitle() + "».");
    }

    private String requiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + ": поле не должно быть пустым.");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new BusinessException(field + ": максимум " + maxLength + " символов.");
        }
        return text;
    }
}
