package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.service.ReportService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1/reports/email")
@RequiredArgsConstructor
public class EmailController {

    private final ReportService reportService;

    @PostMapping("/income")
    public ResponseEntity<Void> emailIncomeExcel() throws IOException, MessagingException {
        reportService.emailIncomeExcel();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/expense")
    public ResponseEntity<Void> emailExpenseExcel() throws IOException, MessagingException {
        reportService.emailExpenseExcel();
        return ResponseEntity.ok().build();
    }
}
