-- V003. Удаление служебной таблицы, созданной Hibernate по ошибке.
--
-- Что делает: удаляет таблицу result_stockdtoselect_batch_product_location.
-- Зачем: класс ResultStockDTOSelectBatchProductLocation был помечен @Entity, хотя служил
-- только для чтения результата запроса. Он заменён интерфейсом-проекцией UnplacedStockRow,
-- таблица больше не используется.
--
-- Идемпотентный, выполняется при старте вместе с V001.

DROP TABLE IF EXISTS result_stockdtoselect_batch_product_location;
