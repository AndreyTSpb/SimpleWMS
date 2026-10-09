-- V001. Справочные данные, необходимые для работы системы.
--
-- Что делает: создаёт роли, администратора, ABC-классы, системные зоны
-- и ячейки (приёмка, отгрузка).
-- Зачем: код рассчитывает на их наличие (ячейка с id = 1 — зона приёмки).
--
-- Выполняется автоматически при старте приложения после создания схемы
-- Hibernate (spring.sql.init). Скрипт идемпотентный: существующие записи
-- не изменяются (ON CONFLICT DO NOTHING), поэтому безопасен при повторном запуске.
-- Имена таблиц и колонок соответствуют именованию Hibernate/Spring
-- (camelCase -> snake_case, «ID» в конце слова -> «id» без подчёркивания).

-- Роли
INSERT INTO roles (roleid, role_name, note) VALUES
    (1, 'Администратор',   'Полный доступ, справочники и пользователи'),
    (2, 'Менеджер склада', 'Документы прихода, заказы, отгрузки'),
    (3, 'Кладовщик',       'Приёмка, размещение, комплектация')
ON CONFLICT (roleid) DO NOTHING;

-- Сотрудник и пользователь-администратор.
-- Пароль: admin (BCrypt). Вход через таблицу users включается на этапе 2.
INSERT INTO employee (employeeid, employee_name, tab_num, del, roleid) VALUES
    (1, 'Администратор системы', '0001', 0, 1)
ON CONFLICT (employeeid) DO NOTHING;

INSERT INTO users (userid, login, pass, del, employeeid) VALUES
    (1, 'admin', '$2a$10$Ht4fAzYGhp/1uGnEzYoBgeRQ56R8SKMo1rbI9oLjJ.D.TQ8jGHROy', false, 1)
ON CONFLICT (userid) DO NOTHING;

-- ABC-классы товаров
INSERT INTO abc (abcid, code, description, del) VALUES
    (1, 'A', 'Высокая оборачиваемость', false),
    (2, 'B', 'Средняя оборачиваемость', false),
    (3, 'C', 'Низкая оборачиваемость',  false),
    (4, 'D', 'Неликвид / редкий спрос', false)
ON CONFLICT (abcid) DO NOTHING;

-- Системные зоны
INSERT INTO zones (zone_id, zone_name, description, code, del) VALUES
    (1, 'Зона приёмки',  'Товар после приёмки до размещения',  'PRM', false),
    (2, 'Зона отгрузки', 'Собранные заказы до отгрузки',       'OTG', false)
ON CONFLICT (zone_id) DO NOTHING;

-- Системные ячейки. id = 1 — ячейка приёмки (используется в коде).
INSERT INTO location (locationid, location_code, "row", x, y, z, capacity, available, del, zoneid) VALUES
    (1, 'PRM-01-01-1-1', '01', 1, 1, 1, 1000, 1, 0, 1),
    (2, 'OTG-01-01-1-1', '01', 1, 1, 1, 1000, 1, 0, 2)
ON CONFLICT (locationid) DO NOTHING;

-- Сдвиг счётчиков id после вставки с явными значениями.
-- Таблицы с GenerationType.IDENTITY:
SELECT setval(pg_get_serial_sequence('roles', 'roleid'),         (SELECT MAX(roleid)     FROM roles));
SELECT setval(pg_get_serial_sequence('employee', 'employeeid'),  (SELECT MAX(employeeid) FROM employee));
SELECT setval(pg_get_serial_sequence('abc', 'abcid'),            (SELECT MAX(abcid)      FROM abc));
SELECT setval(pg_get_serial_sequence('location', 'locationid'),  (SELECT MAX(locationid) FROM location));

-- Таблицы с GenerationType.AUTO (zones, users) используют отдельные
-- последовательности Hibernate с шагом 50. Значение ставится с запасом в шаг,
-- чтобы выделяемый Hibernate диапазон не пересёкся с существующими id.
DO '
DECLARE
    s  record;
    mx bigint;
BEGIN
    FOR s IN
        SELECT c.relname AS seq, t.tbl, t.col
        FROM pg_class c
        JOIN (VALUES (''zones_seq'',       ''zones'', ''zone_id''),
                     (''zone_entity_seq'', ''zones'', ''zone_id''),
                     (''users_seq'',       ''users'', ''userid''),
                     (''user_entity_seq'', ''users'', ''userid'')) AS t(seqname, tbl, col)
          ON c.relname = t.seqname
        WHERE c.relkind = ''S''
    LOOP
        EXECUTE format(''SELECT COALESCE(MAX(%I), 0) FROM %I'', s.col, s.tbl) INTO mx;
        PERFORM setval(quote_ident(s.seq),
                       GREATEST(mx + 50,
                                COALESCE((SELECT last_value FROM pg_sequences WHERE sequencename = s.seq), 1)));
    END LOOP;
END
';
