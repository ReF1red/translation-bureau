BEGIN;

INSERT INTO clients (name, email, phone) VALUES
    ('Анна Смирнова', 'anna@example.com', '+7 900 000-00-01'),
    ('Иван Петров', 'ivan@example.com', '+7 900 000-00-02'),
    ('Мария Иванова', 'maria@example.com', '+7 900 000-00-03'),
    ('ООО Вектор', 'vector@example.com', '+7 900 000-00-04'),
    ('Алексей Соколов', 'alexey@example.com', NULL);

INSERT INTO translation_orders
    (client_id, title, description, source_language, target_language,
     word_count, price, status, created_at, deadline)
VALUES
    ((SELECT id FROM clients WHERE email = 'anna@example.com'),
     'Перевод паспорта', 'Перевод личных данных для подачи документов',
     'Русский', 'Английский', 250, 1500.00, 'NEW', CURRENT_DATE, CURRENT_DATE + 3),
    ((SELECT id FROM clients WHERE email = 'ivan@example.com'),
     'Перевод договора поставки', 'Договор на поставку оборудования',
     'Английский', 'Русский', 3200, 16000.00, 'IN_PROGRESS', CURRENT_DATE - 3, CURRENT_DATE + 4),
    ((SELECT id FROM clients WHERE email = 'maria@example.com'),
     'Перевод диплома', 'Диплом и приложение для поступления',
     'Русский', 'Немецкий', 1200, 7200.00, 'COMPLETED', CURRENT_DATE - 12, CURRENT_DATE - 5),
    ((SELECT id FROM clients WHERE email = 'vector@example.com'),
     'Перевод сайта компании', 'Главная страница и описание услуг',
     'Русский', 'Английский', 4500, 22500.00, 'NEW', CURRENT_DATE, CURRENT_DATE + 10),
    ((SELECT id FROM clients WHERE email = 'alexey@example.com'),
     'Перевод инструкции', 'Инструкция к бытовой технике',
     'Немецкий', 'Русский', 2800, 14000.00, 'CANCELLED', CURRENT_DATE - 7, CURRENT_DATE + 2),
    ((SELECT id FROM clients WHERE email = 'anna@example.com'),
     'Перевод справки', 'Справка с места работы',
     'Русский', 'Французский', 400, 2400.00, 'COMPLETED', CURRENT_DATE - 8, CURRENT_DATE - 3),
    ((SELECT id FROM clients WHERE email = 'ivan@example.com'),
     'Перевод статьи', 'Научная статья об обработке данных',
     'Английский', 'Русский', 5100, 25500.00, 'IN_PROGRESS', CURRENT_DATE - 6, CURRENT_DATE - 1),
    ((SELECT id FROM clients WHERE email = 'maria@example.com'),
     'Перевод резюме', 'Резюме для поиска работы',
     'Русский', 'Английский', 600, 3000.00, 'NEW', CURRENT_DATE, CURRENT_DATE + 2),
    ((SELECT id FROM clients WHERE email = 'vector@example.com'),
     'Перевод договора аренды', 'Договор аренды офисного помещения',
     'Французский', 'Русский', 2100, 12600.00, 'IN_PROGRESS', CURRENT_DATE - 2, CURRENT_DATE + 5),
    ((SELECT id FROM clients WHERE email = 'alexey@example.com'),
     'Перевод письма', 'Деловое письмо партнёру',
     'Русский', 'Испанский', 350, 2100.00, 'CANCELLED', CURRENT_DATE - 4, CURRENT_DATE + 1);

COMMIT;
