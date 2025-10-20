package com.mycompany.granjoyapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GranjoyApp extends JFrame {

    // Archivos donde se guardará la información
    private static final String ANIMALS_FILE = "animals.csv";
    private static final String INVENTORY_FILE = "inventory.csv";
    private static final String LOGS_FILE = "logs.csv";
    private static final String SALES_FILE = "sales.csv";
    private static final String BACKUP_DIR = "backups"; // Carpeta de respaldo

    // Listas de datos cargadas en memoria
    private final List<Animal> animals = new ArrayList<>();
    private final List<InventoryItem> inventory = new ArrayList<>();

    // Modelos de tabla y componentes
    private DefaultTableModel animalsTableModel;
    private JTable animalsTable;
    private DefaultTableModel inventoryTableModel;
    private JTable inventoryTable;
    private DefaultTableModel logsTableModel;
    private JTable logsTable;

    // Etiquetas para mostrar los totales financieros
    private JLabel totalIncomeLabel = new JLabel("Ingresos: 0.00");
    private JLabel totalCostLabel = new JLabel("Costos: 0.00");
    private JLabel totalProfitLabel = new JLabel("Utilidad: 0.00");

    // Constructor principal
    public GranjoyApp() {
        setTitle("Granjoy - Sistema de Gestión de Granja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Centra la ventana

        // Cargar los datos desde los archivos
        loadAllData();

        // Crear pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Animales", buildAnimalsPanel());
        tabs.addTab("Inventario", buildInventoryPanel());
        tabs.addTab("Registros", buildLogsPanel());
        tabs.addTab("Finanzas", buildFinancePanel());

        add(tabs);
    }

    // ============================================================
    // PANEL DE ANIMALES
    // ============================================================
    private JPanel buildAnimalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ---------- Formulario de registro de animales ----------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Registrar Animal"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Especie:"), c);
        c.gridx = 1; JTextField speciesField = new JTextField(12); form.add(speciesField, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Nombre/ID:"), c);
        c.gridx = 1; JTextField nameField = new JTextField(12); form.add(nameField, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Edad (años):"), c);
        c.gridx = 1; JTextField ageField = new JTextField(6); form.add(ageField, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel("Peso (kg):"), c);
        c.gridx = 1; JTextField weightField = new JTextField(8); form.add(weightField, c);

        c.gridx = 0; c.gridy = 4; form.add(new JLabel("Estado de salud:"), c);
        c.gridx = 1; JTextField healthField = new JTextField(12); form.add(healthField, c);

        c.gridx = 0; c.gridy = 5; form.add(new JLabel("Costo acumulado (COP):"), c);
        c.gridx = 1; JTextField costField = new JTextField(10); form.add(costField, c);

        // Botón para registrar animal
        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        JButton addBtn = new JButton("Registrar Animal");
        form.add(addBtn, c);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.WEST);

        // ---------- Panel de búsqueda ----------
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Buscar / Filtrar"));
        searchPanel.add(new JLabel("Texto:"));
        JTextField filterField = new JTextField(20);
        searchPanel.add(filterField);
        JButton filterBtn = new JButton("Aplicar filtro");
        JButton clearFilterBtn = new JButton("Limpiar filtro");
        searchPanel.add(filterBtn);
        searchPanel.add(clearFilterBtn);
        top.add(searchPanel, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);

        // ---------- Tabla de animales ----------
        String[] cols = {"ID","Especie","Nombre","Edad","Peso(kg)","Salud","Ingreso","Costo"};
        animalsTableModel = new DefaultTableModel(cols, 0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        animalsTable = new JTable(animalsTableModel);
        refreshAnimalsTable();
        panel.add(new JScrollPane(animalsTable), BorderLayout.CENTER);

        // ---------- Botones de acción ----------
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JButton deleteBtn = new JButton("Eliminar animal");
        JButton feedBtn = new JButton("Registrar alimentación");
        JButton medBtn = new JButton("Registrar medicación");
        JButton updateWeightBtn = new JButton("Actualizar peso");
        JButton sellBtn = new JButton("Registrar venta");
        actions.add(deleteBtn);
        actions.add(Box.createVerticalStrut(10));
        actions.add(feedBtn);
        actions.add(Box.createVerticalStrut(6));
        actions.add(medBtn);
        actions.add(Box.createVerticalStrut(6));
        actions.add(updateWeightBtn);
        actions.add(Box.createVerticalStrut(12));
        actions.add(sellBtn);
        panel.add(actions, BorderLayout.EAST);

        // ---------- Eventos ----------
        addBtn.addActionListener(e -> {
            String species = speciesField.getText().trim();
            String name = nameField.getText().trim();
            int age = parseIntOrZero(ageField.getText().trim());
            double weight = parseDoubleOrZero(weightField.getText().trim());
            String health = healthField.getText().trim();
            double cost = parseDoubleOrZero(costField.getText().trim());
            if (species.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La especie y el nombre son obligatorios.");
                return;
            }
            Animal a = new Animal(generateAnimalId(), species, name, age, weight, health, today(), cost);
            animals.add(a);
            saveAnimals();
            refreshAnimalsTable();
            // Limpiar campos
            speciesField.setText(""); nameField.setText("");
            ageField.setText(""); weightField.setText("");
            healthField.setText(""); costField.setText("");
        });

        deleteBtn.addActionListener(e -> {
            int row = animalsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleccione un animal."); return; }
            String id = (String) animalsTableModel.getValueAt(row, 0);
            animals.removeIf(a -> a.id.equals(id));
            saveAnimals();
            refreshAnimalsTable();
        });

        feedBtn.addActionListener(e -> registerAnimalAction("ALIMENTACIÓN"));
        medBtn.addActionListener(e -> registerAnimalAction("MEDICACIÓN"));

        updateWeightBtn.addActionListener(e -> {
            int row = animalsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleccione un animal."); return; }
            String id = (String) animalsTableModel.getValueAt(row, 0);
            String s = JOptionPane.showInputDialog(this, "Nuevo peso (kg):");
            if (s == null) return;
            double w = parseDoubleOrZero(s.trim());
            Optional<Animal> opt = animals.stream().filter(a -> a.id.equals(id)).findFirst();
            if (opt.isPresent()) {
                opt.get().weight = w;
                saveAnimals();
                refreshAnimalsTable();
                appendLog(opt.get().id, "ACTUALIZAR_PESO", "Peso actualizado a " + w + " kg");
            }
        });

        sellBtn.addActionListener(e -> {
            int row = animalsTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleccione un animal para vender."); return; }
            String id = (String) animalsTableModel.getValueAt(row, 0);
            Optional<Animal> opt = animals.stream().filter(a -> a.id.equals(id)).findFirst();
            if (!opt.isPresent()) return;
            Animal a = opt.get();
            String sPrice = JOptionPane.showInputDialog(this, "Precio de venta (COP):");
            if (sPrice == null) return;
            double salePrice = parseDoubleOrZero(sPrice.trim());
            double profit = salePrice - a.cost;
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(SALES_FILE), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                bw.write(toCSV(new String[]{nowTimestamp(), a.id, a.name, a.species, String.valueOf(salePrice), String.valueOf(a.cost), String.valueOf(profit)}));
                bw.newLine();
            } catch (IOException ex) { ex.printStackTrace(); }
            appendLog(a.id, "VENTA", "Vendido a " + salePrice + " COP");
            animals.remove(a);
            saveAnimals();
            refreshAnimalsTable();
            JOptionPane.showMessageDialog(this, "Animal vendido. Utilidad: " + profit + " COP");
        });

        filterBtn.addActionListener(e -> {
            String q = filterField.getText().trim().toLowerCase();
            refreshAnimalsTableFiltered(q);
        });
        clearFilterBtn.addActionListener(e -> refreshAnimalsTable());

        return panel;
    }

    // ============================================================
    // PANEL DE INVENTARIO
    // ============================================================
    private JPanel buildInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Inventario"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Insumo:"), c);
        c.gridx = 1; JTextField itemField = new JTextField(12); form.add(itemField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Cantidad:"), c);
        c.gridx = 1; JTextField qtyField = new JTextField(8); form.add(qtyField, c);
        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Unidad:"), c);
        c.gridx = 1; JTextField unitField = new JTextField(8); form.add(unitField, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        JButton addBtn = new JButton("Añadir o actualizar");
        form.add(addBtn, c);
        panel.add(form, BorderLayout.NORTH);

        String[] cols = {"Insumo","Cantidad","Unidad"};
        inventoryTableModel = new DefaultTableModel(cols, 0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        inventoryTable = new JTable(inventoryTableModel);
        refreshInventoryTable();
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton reduceBtn = new JButton("Usar insumo");
        JButton deleteBtn = new JButton("Eliminar");
        actions.add(reduceBtn); actions.add(deleteBtn);
        panel.add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String item = itemField.getText().trim();
            double qty = parseDoubleOrZero(qtyField.getText().trim());
            String unit = unitField.getText().trim();
            if (item.isEmpty() || qty <= 0) return;
            Optional<InventoryItem> opt = inventory.stream().filter(i -> i.item.equalsIgnoreCase(item)).findFirst();
            if (opt.isPresent()) opt.get().quantity += qty;
            else inventory.add(new InventoryItem(item, qty, unit));
            saveInventory();
            refreshInventoryTable();
        });

        reduceBtn.addActionListener(e -> {
            int row = inventoryTable.getSelectedRow();
            if (row == -1) return;
            String item = (String) inventoryTableModel.getValueAt(row,0);
            String s = JOptionPane.showInputDialog(this, "Cantidad a reducir:");
            double qty = parseDoubleOrZero(s);
            for (InventoryItem i : inventory) if (i.item.equalsIgnoreCase(item)) i.quantity -= qty;
            saveInventory();
            refreshInventoryTable();
        });

        deleteBtn.addActionListener(e -> {
            int row = inventoryTable.getSelectedRow();
            if (row == -1) return;
            String item = (String) inventoryTableModel.getValueAt(row,0);
            inventory.removeIf(i -> i.item.equalsIgnoreCase(item));
            saveInventory();
            refreshInventoryTable();
        });

        return panel;
    }

    // ============================================================
    // PANEL DE REGISTROS
    // ============================================================
    private JPanel buildLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Fecha","AnimalID","Acción","Detalle"};
        logsTableModel = new DefaultTableModel(cols,0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        logsTable = new JTable(logsTableModel);
        refreshLogsTable();
        panel.add(new JScrollPane(logsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton clearBtn = new JButton("Borrar registros");
        JButton backupBtn = new JButton("Crear respaldo");
        actions.add(clearBtn); actions.add(backupBtn);
        panel.add(actions, BorderLayout.SOUTH);

        clearBtn.addActionListener(e -> {
            try { Files.deleteIfExists(Paths.get(LOGS_FILE)); } catch (IOException ex) {}
            refreshLogsTable();
        });

        backupBtn.addActionListener(e -> {
            try {
                createBackup();
                JOptionPane.showMessageDialog(this,"Respaldo creado en carpeta 'backups'");
            } catch (IOException ex) { JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage()); }
        });

        return panel;
    }

    // ============================================================
    // PANEL DE FINANZAS
    // ============================================================
    private JPanel buildFinancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel sums = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sums.setBorder(BorderFactory.createTitledBorder("Resumen financiero"));
        updateFinanceLabels();
        sums.add(totalIncomeLabel);
        sums.add(Box.createHorizontalStrut(20));
        sums.add(totalCostLabel);
        sums.add(Box.createHorizontalStrut(20));
        sums.add(totalProfitLabel);
        panel.add(sums, BorderLayout.NORTH);
        return panel;
    }

    // FUNCIONES AUXILIARES

    private void loadAllData() {
        loadAnimals();
        loadInventory();
        updateFinanceLabels();
    }

    private void loadAnimals() {
        animals.clear();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(ANIMALS_FILE))) {
            String line; while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 8)
                    animals.add(new Animal(p[0],p[1],p[2],Integer.parseInt(p[3]),
                    Double.parseDouble(p[4]),p[5],p[6],Double.parseDouble(p[7])));
            }
        } catch (IOException ignored) {}
    }

    private void saveAnimals() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(ANIMALS_FILE))) {
            for (Animal a : animals)
                bw.write(toCSV(new String[]{a.id,a.species,a.name,String.valueOf(a.age),
                String.valueOf(a.weight),a.health,a.entryDate,String.valueOf(a.cost)}));
        } catch (IOException ignored) {}
    }

    private void loadInventory() {
        inventory.clear();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(INVENTORY_FILE))) {
            String line; while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 3)
                    inventory.add(new InventoryItem(p[0],Double.parseDouble(p[1]),p[2]));
            }
        } catch (IOException ignored) {}
    }

    private void saveInventory() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(INVENTORY_FILE))) {
            for (InventoryItem i : inventory)
                bw.write(toCSV(new String[]{i.item,String.valueOf(i.quantity),i.unit}));
        } catch (IOException ignored) {}
    }

    private void refreshAnimalsTable() {
        animalsTableModel.setRowCount(0);
        for (Animal a : animals)
            animalsTableModel.addRow(new Object[]{a.id,a.species,a.name,a.age,a.weight,a.health,a.entryDate,a.cost});
    }

    private void refreshAnimalsTableFiltered(String q) {
        animalsTableModel.setRowCount(0);
        for (Animal a : animals)
            if (a.name.toLowerCase().contains(q) || a.species.toLowerCase().contains(q))
                animalsTableModel.addRow(new Object[]{a.id,a.species,a.name,a.age,a.weight,a.health,a.entryDate,a.cost});
    }

    private void refreshInventoryTable() {
        inventoryTableModel.setRowCount(0);
        for (InventoryItem i : inventory)
            inventoryTableModel.addRow(new Object[]{i.item,i.quantity,i.unit});
    }

    private void refreshLogsTable() {
        logsTableModel.setRowCount(0);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(LOGS_FILE))) {
            String line; while ((line = br.readLine()) != null) {
                String[] p = line.split(",",4);
                logsTableModel.addRow(p);
            }
        } catch (IOException ignored) {}
    }

    private void appendLog(String id, String action, String detail) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(LOGS_FILE), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(toCSV(new String[]{nowTimestamp(),id,action,detail}));
            bw.newLine();
        } catch (IOException ignored) {}
        refreshLogsTable();
    }

    private void createBackup() throws IOException {
        Files.createDirectories(Paths.get(BACKUP_DIR));
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        for (String f : List.of(ANIMALS_FILE, INVENTORY_FILE, SALES_FILE, LOGS_FILE)) {
            Path src = Paths.get(f);
            if (Files.exists(src)) {
                Files.copy(src, Paths.get(BACKUP_DIR, time + "_" + f), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void updateFinanceLabels() {
        double totalIncome = 0, totalCost = 0;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(SALES_FILE))) {
            String line; while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 7) {
                    totalIncome += Double.parseDouble(p[4]);
                    totalCost += Double.parseDouble(p[5]);
                }
            }
        } catch (IOException ignored) {}
        double profit = totalIncome - totalCost;
        totalIncomeLabel.setText("Ingresos: " + totalIncome);
        totalCostLabel.setText("Costos: " + totalCost);
        totalProfitLabel.setText("Utilidad: " + profit);
    }

    private void registerAnimalAction(String tipo) {
        int row = animalsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Seleccione un animal."); return; }
        String id = (String) animalsTableModel.getValueAt(row, 0);
        String detalle = JOptionPane.showInputDialog(this, "Detalle de " + tipo + ":");
        if (detalle != null) appendLog(id, tipo, detalle);
    }

    private static String toCSV(String[] arr) {
        return String.join(",", arr);
    }

    private static int parseIntOrZero(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private static double parseDoubleOrZero(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private static String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private String generateAnimalId() {
        return "A" + (animals.size() + 1);
    }

       // CLASES INTERNAS (MODELOS DE DATOS)
   
    static class Animal {
        String id, species, name, health, entryDate;
        int age;
        double weight, cost;

        Animal(String id, String species, String name, int age, double weight, String health, String entryDate, double cost) {
            this.id = id; this.species = species; this.name = name;
            this.age = age; this.weight = weight; this.health = health;
            this.entryDate = entryDate; this.cost = cost;
        }
    }

    static class InventoryItem {
        String item, unit;
        double quantity;
        InventoryItem(String item, double quantity, String unit) {
            this.item = item; this.quantity = quantity; this.unit = unit;
        }
    }

       // MÉTODO PRINCIPAL
   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GranjoyApp().setVisible(true));
    }
}
