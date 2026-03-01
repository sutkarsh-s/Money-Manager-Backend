package in.utkarshsingh.money.manager.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ExcelService excelService;
    private final AnalyticsService analyticsService;
    private final EmailService emailService;
    private final UserResolverService userResolverService;

    public void writeIncomeExcel(OutputStream outputStream) throws IOException {
        List<IncomeDTO> incomes = getCurrentMonthIncomes();
        excelService.writeIncomesToExcel(outputStream, incomes);
    }

    public void writeExpenseExcel(OutputStream outputStream) throws IOException {
        List<ExpenseDTO> expenses = getCurrentMonthExpenses();
        excelService.writeExpensesToExcel(outputStream, expenses);
    }

    public void writeIncomeCsv(OutputStream outputStream) throws IOException {
        List<IncomeDTO> incomes = getCurrentMonthIncomes();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"S.No", "Name", "Category", "Amount", "Date"});
            int i = 1;
            for (IncomeDTO income : incomes) {
                writer.writeNext(new String[]{
                        String.valueOf(i++),
                        income.getName(),
                        income.getCategoryName() != null ? income.getCategoryName() : "N/A",
                        income.getAmount() != null ? income.getAmount().toPlainString() : "0",
                        income.getDate() != null ? income.getDate().toString() : ""
                });
            }
        }
    }

    public void writeExpenseCsv(OutputStream outputStream) throws IOException {
        List<ExpenseDTO> expenses = getCurrentMonthExpenses();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"S.No", "Name", "Category", "Amount", "Date"});
            int i = 1;
            for (ExpenseDTO expense : expenses) {
                writer.writeNext(new String[]{
                        String.valueOf(i++),
                        expense.getName(),
                        expense.getCategoryName() != null ? expense.getCategoryName() : "N/A",
                        expense.getAmount() != null ? expense.getAmount().toPlainString() : "0",
                        expense.getDate() != null ? expense.getDate().toString() : ""
                });
            }
        }
    }

    public void writeIncomePdf(OutputStream outputStream) {
        List<IncomeDTO> incomes = getCurrentMonthIncomes();
        writePdf(outputStream, "Income Report", incomes.stream()
                .map(i -> new String[]{i.getName(),
                        i.getCategoryName() != null ? i.getCategoryName() : "N/A",
                        i.getAmount() != null ? i.getAmount().toPlainString() : "0",
                        i.getDate() != null ? i.getDate().toString() : ""})
                .toList());
    }

    public void writeExpensePdf(OutputStream outputStream) {
        List<ExpenseDTO> expenses = getCurrentMonthExpenses();
        writePdf(outputStream, "Expense Report", expenses.stream()
                .map(e -> new String[]{e.getName(),
                        e.getCategoryName() != null ? e.getCategoryName() : "N/A",
                        e.getAmount() != null ? e.getAmount().toPlainString() : "0",
                        e.getDate() != null ? e.getDate().toString() : ""})
                .toList());
    }

    public void emailIncomeExcel() throws IOException, MessagingException {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(baos, getCurrentMonthIncomes());
        emailService.sendEmailWithAttachment(profile.getEmail(),
                "Your Income Excel Report",
                "Please find attached your income report",
                baos.toByteArray(), "income.xlsx");
        log.info("Income Excel report sent to: {}", profile.getEmail());
    }

    public void emailExpenseExcel() throws IOException, MessagingException {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeExpensesToExcel(baos, getCurrentMonthExpenses());
        emailService.sendEmailWithAttachment(profile.getEmail(),
                "Your Expense Excel Report",
                "Please find attached your expense report.",
                baos.toByteArray(), "expenses.xlsx");
        log.info("Expense Excel report sent to: {}", profile.getEmail());
    }

    private List<IncomeDTO> getCurrentMonthIncomes() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        return analyticsService.getIncomeDataForRange(profile.getId(), startDate, endDate);
    }

    private List<ExpenseDTO> getCurrentMonthExpenses() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        return analyticsService.getExpenseDataForRange(profile.getId(), startDate, endDate);
    }

    private void writePdf(OutputStream outputStream, String title, List<String[]> rows) {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(88, 28, 135));
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        PdfPTable table = new PdfPTable(new float[]{1f, 3f, 2f, 2f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        String[] headers = {"#", "Name", "Category", "Amount", "Date"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(88, 28, 135));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
        int idx = 1;
        for (String[] row : rows) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(idx++), dataFont)));
            for (String val : row) {
                PdfPCell cell = new PdfPCell(new Phrase(val, dataFont));
                cell.setPadding(6);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
    }
}
