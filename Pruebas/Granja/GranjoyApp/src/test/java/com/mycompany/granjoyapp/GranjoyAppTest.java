package com.mycompany.granjoyapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GranjoyAppTest {

    @Test
    void testInicializacion() {
        GranjoyApp app = new GranjoyApp();
        assertNotNull(app);
    }
    
}
