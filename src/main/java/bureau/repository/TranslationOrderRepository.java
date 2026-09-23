package bureau.repository;

import bureau.model.TranslationOrder;

import java.util.List;

public interface TranslationOrderRepository {
    TranslationOrder create(TranslationOrder order);
    List<TranslationOrder> findAll();
    TranslationOrder findById(long id);
    boolean update(TranslationOrder order);
    boolean delete(long id);
}
