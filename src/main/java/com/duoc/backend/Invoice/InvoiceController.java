package com.duoc.backend.Invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return (List<Invoice>) invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    //Nueva funcionalidad semana4:detalle de facturacion
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getDetailedInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getDetailedInvoice(id));
    }

    @PostMapping
    public Invoice saveInvoice(@RequestBody Invoice invoice) {
        return invoiceService.saveInvoice(invoice);
    }

    //Nueva funcionalidad semana5: envío o impresion de facturación
    @PostMapping("/{id}/send")
    public ResponseEntity<String> sendInvoice(@PathVariable Long id) {
        invoiceService.sendInvoiceByEmail(id);
        return ResponseEntity.ok("Factura enviada correctamente");
    }

    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }
}
