package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ExcelController {

    private final ReportService reportService;

    @GetMapping("/download/income")
    public void downloadIncome(HttpServletResponse response,
                               @RequestParam(defaultValue = "xlsx") String format) throws IOException {
        switch (format.toLowerCase()) {
            case "csv" -> {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=income.csv");
                reportService.writeIncomeCsv(response.getOutputStream());
            }
            case "pdf" -> {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=income.pdf");
                reportService.writeIncomePdf(response.getOutputStream());
            }
            default -> {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=income.xlsx");
                reportService.writeIncomeExcel(response.getOutputStream());
            }
        }
    }

    @GetMapping("/download/expense")
    public void downloadExpense(HttpServletResponse response,
                                @RequestParam(defaultValue = "xlsx") String format) throws IOException {
        switch (format.toLowerCase()) {
            case "csv" -> {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=expense.csv");
                reportService.writeExpenseCsv(response.getOutputStream());
            }
            case "pdf" -> {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=expense.pdf");
                reportService.writeExpensePdf(response.getOutputStream());
            }
            default -> {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=expense.xlsx");
                reportService.writeExpenseExcel(response.getOutputStream());
            }
        }
    }

    @GetMapping("/excel/download/income")
    public void legacyDownloadIncomeExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=income.xlsx");
        reportService.writeIncomeExcel(response.getOutputStream());
    }

    @GetMapping("/excel/download/expense")
    public void legacyDownloadExpenseExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=expense.xlsx");
        reportService.writeExpenseExcel(response.getOutputStream());
    }
}
