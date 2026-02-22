package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.entity.ProfileEntity;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ExcelService excelService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final EmailService emailService;
    private final ProfileService profileService;

    public void writeIncomeExcel(OutputStream outputStream) throws IOException {
        excelService.writeIncomesToExcel(outputStream, incomeService.getCurrentMonthIncomesForCurrentUser());
    }

    public void writeExpenseExcel(OutputStream outputStream) throws IOException {
        excelService.writeExpensesToExcel(outputStream, expenseService.getCurrentMonthExpensesForCurrentUser());
    }

    public void emailIncomeExcel() throws IOException, MessagingException {
        ProfileEntity profile = profileService.getCurrentProfile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeIncomesToExcel(baos, incomeService.getCurrentMonthIncomesForCurrentUser());
        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Income Excel Report",
                "Please find attached your income report",
                baos.toByteArray(),
                "income.xlsx"
        );
        log.info("Income Excel report sent to: {}", profile.getEmail());
    }

    public void emailExpenseExcel() throws IOException, MessagingException {
        ProfileEntity profile = profileService.getCurrentProfile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        excelService.writeExpensesToExcel(baos, expenseService.getCurrentMonthExpensesForCurrentUser());
        emailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Expense Excel Report",
                "Please find attached your expense report.",
                baos.toByteArray(),
                "expenses.xlsx"
        );
        log.info("Expense Excel report sent to: {}", profile.getEmail());
    }
}
