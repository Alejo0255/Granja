package com.mycompany.granjoyapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Nota: Importamos la clase interna Animal de GranjoyApp
import com.mycompany.granjoyapp.GranjoyApp.Animal;

public class AnimalTest {

    @Test
    void testAnimalCreation() {
        Animal a = new Animal("A1", "Vaca", "Lechera", 3, 500.0, "Buena", "2025-10-20", 200000.0);

        assertEquals("A1", a.getId());
        assertEquals("Vaca", a.getSpecies());
        assertEquals("Lechera", a.getName());
        assertEquals(3, a.getAge());
        assertEquals(500.0, a.getWeight());
        assertEquals("Buena", a.getHealth());
        assertEquals("2025-10-20", a.getEntryDate());
        assertEquals(200000.0, a.getCost());
        
    }
    @Test
void testCamposNoVacios() {
    GranjoyApp.Animal a = new GranjoyApp.Animal(
        "A3", "Gallina", "Blanquita", 1, 3.5, "Excelente", "2025-10-20", 30000.0
    );

    assertNotNull(a.getId());
    assertFalse(a.getName().isEmpty());
    assertFalse(a.getSpecies().isEmpty());
}
@Test
void testValoresPositivos() {
    GranjoyApp.Animal a = new GranjoyApp.Animal(
        "A2", "Cerdo", "Pinky", 2, 120.0, "Sano", "2025-10-19", 150000.0
    );

    assertTrue(a.getWeight() > 0, "El peso debe ser positivo");
    assertTrue(a.getCost() > 0, "El costo debe ser positivo");
}

}
