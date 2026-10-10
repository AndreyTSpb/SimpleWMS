package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.service.PlacementCellPicker.Cell;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlacementCellPickerTest {

    // SC1 — ближняя зона для A/B, SC2 — для C/D; вместимость 100 л
    private static final Cell A1 = new Cell(101, "SC1-01-01-1-1", 3, "SC1", "01", 1, 1, 1, 100);
    private static final Cell A2 = new Cell(102, "SC1-01-02-1-1", 3, "SC1", "01", 2, 1, 1, 100);
    private static final Cell A3 = new Cell(103, "SC1-02-01-1-1", 3, "SC1", "02", 1, 1, 1, 100);
    private static final Cell B1 = new Cell(201, "SC2-01-01-1-1", 4, "SC2", "01", 1, 1, 1, 100);
    private static final Map<String, String> ABC = Map.of("A", "SC1", "C", "SC2");

    @Test
    void prefersCellWithSameProduct() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A1, A2, A3), Map.of(102, 30.0), ABC);
        assertThat(picker.pick("A", 50, Set.of(102))).map(Cell::id).contains(102);
    }

    @Test
    void skipsSameProductCellWithoutSpace() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A1, A2), Map.of(102, 80.0), ABC);
        assertThat(picker.pick("A", 50, Set.of(102))).map(Cell::id).contains(101);
    }

    @Test
    void takesNearestEmptyCellInPreferredZone() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A3, B1, A2, A1), Map.of(101, 10.0), ABC);
        // A1 занята частично, ближайшая пустая в SC1 — A2 (ряд 01, x=2)
        assertThat(picker.pick("A", 20, Set.of())).map(Cell::id).contains(102);
        // для класса C предпочтительна SC2
        assertThat(picker.pick("C", 20, Set.of())).map(Cell::id).contains(201);
    }

    @Test
    void reservesSpaceForFollowingLines() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A1), Map.of(), ABC);
        assertThat(picker.pick("A", 60, Set.of())).map(Cell::id).contains(101);
        assertThat(picker.pick("A", 60, Set.of())).isEmpty();
        assertThat(picker.pick("A", 40, Set.of())).map(Cell::id).contains(101);
    }

    @Test
    void fallsBackToOtherZoneWhenPreferredIsFull() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A1, B1), Map.of(101, 100.0), ABC);
        assertThat(picker.pick("A", 10, Set.of())).map(Cell::id).contains(201);
    }

    @Test
    void returnsEmptyWhenNothingFits() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(A1, B1), Map.of(), ABC);
        assertThat(picker.pick("A", 150, Set.of())).isEmpty();
    }

    @Test
    void worksWithoutAbcSettings() {
        PlacementCellPicker picker = new PlacementCellPicker(List.of(B1, A1), Map.of(), null);
        assertThat(picker.pick(null, 10, Set.of())).map(Cell::id).contains(101);
    }
}
