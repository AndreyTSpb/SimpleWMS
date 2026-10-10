package com.tynyany.simplewmsv2.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Подбор ячейки хранения для размещения партии.
 *
 * Порядок поиска (внутри каждого шага — сначала зона, предпочтительная для ABC-класса товара,
 * затем ближайшие ячейки: по ряду, x, y, z):
 * 1) ячейка, где уже лежит этот товар и хватает свободного объёма;
 * 2) пустая ячейка;
 * 3) любая ячейка, где хватает свободного объёма.
 *
 * Объёмы — в литрах. Занятость учитывает остатки и уже запланированные размещения;
 * выбранная ячейка резервируется, чтобы следующие строки того же маршрута это учитывали.
 */
public class PlacementCellPicker {

    /** Ячейка хранения. */
    public record Cell(int id, String code, int zoneId, String zoneCode,
                       String row, int x, int y, int z, double capacity) {}

    private static final Comparator<Cell> NEAREST = Comparator
            .comparing((Cell c) -> c.row() == null ? "" : c.row())
            .thenComparingInt(Cell::x)
            .thenComparingInt(Cell::y)
            .thenComparingInt(Cell::z)
            .thenComparingInt(Cell::id);

    private final List<Cell> cells;
    private final Map<Integer, Double> used;
    private final Map<String, String> abcZones;

    /**
     * @param cells    активные ячейки хранения (без системных ячеек приёмки и отгрузки)
     * @param used     занятый и запланированный объём по id ячейки
     * @param abcZones предпочтительная зона (код) для ABC-класса (код A/B/C/D)
     */
    public PlacementCellPicker(List<Cell> cells, Map<Integer, Double> used, Map<String, String> abcZones) {
        this.cells = cells.stream().sorted(NEAREST).toList();
        this.used = new HashMap<>(used);
        this.abcZones = abcZones == null ? Map.of() : abcZones;
    }

    public Optional<Cell> find(int cellId) {
        return cells.stream().filter(c -> c.id() == cellId).findFirst();
    }

    public double free(Cell cell) {
        return cell.capacity() - used.getOrDefault(cell.id(), 0.0);
    }

    public void reserve(int cellId, double volume) {
        used.merge(cellId, volume, Double::sum);
    }

    /**
     * @param abcCode          ABC-класс товара (может быть null)
     * @param volume           объём размещаемой партии, л
     * @param cellsWithProduct ячейки, где уже лежит этот товар
     */
    public Optional<Cell> pick(String abcCode, double volume, Set<Integer> cellsWithProduct) {
        String preferredZone = abcCode == null ? null : abcZones.get(abcCode);
        Predicate<Cell> fits = c -> free(c) >= volume;
        Predicate<Cell> empty = c -> used.getOrDefault(c.id(), 0.0) <= 0.0;

        List<Predicate<Cell>> steps = List.of(
                c -> cellsWithProduct.contains(c.id()) && fits.test(c),
                c -> empty.test(c) && fits.test(c),
                fits);

        for (Predicate<Cell> step : steps) {
            Optional<Cell> found = firstMatch(step, preferredZone);
            if (found.isPresent()) {
                reserve(found.get().id(), volume);
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<Cell> firstMatch(Predicate<Cell> condition, String preferredZone) {
        if (preferredZone != null) {
            Optional<Cell> inPreferred = cells.stream()
                    .filter(c -> preferredZone.equals(c.zoneCode()))
                    .filter(condition)
                    .findFirst();
            if (inPreferred.isPresent()) return inPreferred;
        }
        return cells.stream().filter(condition).findFirst();
    }
}
