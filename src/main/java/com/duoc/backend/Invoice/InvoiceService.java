package com.duoc.backend.Invoice;

import com.duoc.backend.Care.Care;
import com.duoc.backend.Care.CareRepository;
import com.duoc.backend.Medication.Medication;
import com.duoc.backend.Medication.MedicationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private CareRepository careRepository;

    public Iterable<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public Invoice saveInvoice(Invoice invoice) {
        // Validar que los medicamentos existen
        List<Medication> validMedications = StreamSupport.stream(
                medicationRepository.findAllById(
                        invoice.getMedications().stream().map(Medication::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validMedications.size() != invoice.getMedications().size()) {
            throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
        }

        // Validar que los servicios existen
        List<Care> validCares = StreamSupport.stream(
                careRepository.findAllById(
                        invoice.getCares().stream().map(Care::getId).collect(Collectors.toList())
                ).spliterator(), false
        ).collect(Collectors.toList());
        if (validCares.size() != invoice.getCares().size()) {
            throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
        }

        // Calcular el costo total basado en los servicios y medicamentos asociados
        double totalCareCost = validCares.stream()
                .mapToDouble(Care::getCost)
                .sum();

        double totalMedicationCost = validMedications.stream()
                .mapToDouble(Medication::getCost)
                .sum();

        invoice.setTotalCost(totalCareCost + totalMedicationCost);

        // Guardar la factura en el repositorio
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    //Nueva funcionalidad semana4: generacion de facturas detalladas
    public Map<String, Object> getDetailedInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);

        if (invoice == null) {
            throw new RuntimeException("Factura no encontrada");
        }

        Map<String, Object> response = new HashMap<>();

        response.put("facturaId", invoice.getId());
        response.put("total", invoice.getTotalCost());

        response.put("medicamentos", invoice.getMedications().stream().map(m -> {
            Map<String, Object> med = new HashMap<>();
            med.put("nombre", m.getName());
            med.put("precio", m.getCost());
            return med;
        }).collect(Collectors.toList()));

        response.put("servicios", invoice.getCares().stream().map(c -> {
            Map<String, Object> care = new HashMap<>();
            care.put("nombre", c.getName());
            care.put("precio", c.getCost());
            return care;
        }).collect(Collectors.toList()));

        return response;
    }

    //Nueva funcionalidad semana5: enviar el invoice de manera simulada
    public void sendInvoiceByEmail(Long id) {
        Invoice invoice = getInvoiceById(id);

        // simulación
        System.out.println("Enviando factura ID: " + id);

        if (invoice == null) {
            throw new RuntimeException("Factura no encontrada");
        }
    }
}