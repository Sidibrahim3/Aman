package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.ExportDataDto;
import com.sidibrahim.Aman.dto.ExportTransactionDto;
import com.sidibrahim.Aman.dto.TransactionDto;
import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.enums.TransactionType;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.mapper.TransactionMapper;
import com.sidibrahim.Aman.repository.TransactionRepository;
import com.sidibrahim.Aman.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper transactionMapper, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionDto save(Transaction transaction, @AuthenticationPrincipal User user) {
        transaction.setAgency(user.getAgency());
        transaction.setCreateDate(LocalDateTime.now());
        transaction.setAgent(user);
        transaction.setCreateDate(LocalDateTime.now());
        transaction.setUpdateDate(LocalDateTime.now());
        return transactionMapper
                .toTransactionDto(transactionRepository
                        .save(transaction));
    }
    public Page<TransactionDto> findAll(int page, int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       /* User user = userRepository.findUserByPhoneNumber(auth.getName()).get();
        Long agencyId;
        if(user.getAgency()!=null){
            agencyId = user.getAgency().getId();
        }*/
        /*else {
            throw new GenericException("Agency Null ");
        }*/
        User user = (User) auth.getPrincipal();
        //return transactionMapper.toTransactionDtos(transactionRepository.findAllActiveTransactionsByAgencyId(agencyId,PageRequest.of(page, size)));
        return transactionMapper.toTransactionDtos(transactionRepository.findByAgentIdAndNotDeletedOrderByUpdateDateDesc(user.getId(),PageRequest.of(page,size)));
    }
    public Page<TransactionDto> findAllDeletedTransactions(int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByPhoneNumber(auth.getName()).get();
        Long agencyId = user.getAgency().getId();
        return transactionMapper.toTransactionDtos(transactionRepository.findAllDeletedTransactionsByAgencyId(agencyId,PageRequest.of(page, size)));
    }

    public TransactionDto findById(Long id) {
        return transactionMapper
                .toTransactionDto(transactionRepository
                        .findById(id)
                        .orElseThrow(() -> new GenericException("Transaction Not Found")));
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new GenericException("Transaction Not Found"));
        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);
    }

    public List<TransactionDto> findByAgentId(Long id) {
        return transactionMapper.toTransactionDtos(transactionRepository.findByAgentId(id));
    }

    public List<TransactionDto> findByAgencyId(Long id){
        return transactionMapper.toTransactionDtos(transactionRepository.findByAgency_Id(id));
    }

    public List<TransactionDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findTransactionsByDateRange(startDate, endDate);
        return transactionMapper.toTransactionDtos(transactions);
    }

    public List<TransactionDto> findByDateRangeAndType(LocalDateTime startDate, LocalDateTime endDate, TransactionType type) {
        List<Transaction> transactions = transactionRepository.findTransactionsByDateRangeAndType(startDate, endDate, type);
        return transactionMapper.toTransactionDtos(transactions);
    }

    public List<TransactionDto> getTodayTransactions() {
        List<Transaction> transactions = transactionRepository.findTransactionsForToday();
        return transactionMapper.toTransactionDtos(transactions);
    }

    public List<TransactionDto> getThisMonthTransactions() {
        List<Transaction> transactions = transactionRepository.findTransactionsForCurrentMonth();
        return transactionMapper.toTransactionDtos(transactions);
    }

    public Long countTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.countTransactionsByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalEarningsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.sumEarningsByDateRange(startDate, endDate);
    }

    public void softDeleteTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        transactionRepository.softDeleteTransactionsByDateRange(startDate, endDate);
    }

    public List<TransactionDto> exportTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findTransactionsForExport(startDate, endDate);
        return transactionMapper.toTransactionDtos(transactions);
    }

    public List<TransactionDto> findByCustomerPhoneNumber(String phoneNumber) {
        List<Transaction> transactions = transactionRepository.findTransactionsByCustomerPhoneNumber(phoneNumber);
        return transactionMapper.toTransactionDtos(transactions);
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            User user = (User) authentication.getPrincipal();
            log.info("user id :=>"+user.getId());
            return user.getId();

        }
        throw new GenericException("User not authenticated");
    }

    public ExportDataDto prepareExportData(LocalDateTime startDate, LocalDateTime endDate) {
        Long userId = getAuthenticatedUserId(); // Get the authenticated user's ID
        List<Transaction> transactions = transactionRepository.findTransactionsByDateRangeAndUserId(startDate, endDate, userId);

        // Calculate total earnings
        BigDecimal totalEarning = transactions.stream()
                .map(Transaction::getEarn)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total deposits and withdrawals
        BigDecimal totalDeposits = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DEPOSIT)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.WITHDRAWAL)
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate net cash
        BigDecimal netCash = totalDeposits.subtract(totalWithdrawals);

        List<ExportTransactionDto> exportData = transactions.stream()
                .map(transaction -> new ExportTransactionDto(
                        transaction.getAmount(),
                        transaction.getReference(),
                        transaction.getType(),
                        transaction.getCustomerPhoneNumber(),
                        transaction.getEarn()
                ))
                .collect(Collectors.toList());

        return new ExportDataDto(exportData, totalEarning, totalDeposits, totalWithdrawals, netCash);
    }


    public byte[] exportToExcel(LocalDateTime startDate, LocalDateTime endDate) {
        ExportDataDto exportDataDto = prepareExportData(startDate, endDate);
        List<ExportTransactionDto> transactions = exportDataDto.getTransactions();
        BigDecimal totalEarnings = exportDataDto.getTotalEarning();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Amount", "Reference", "Phone Number", "Type", "Earn"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (ExportTransactionDto transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getAmount().toPlainString());
                row.createCell(1).setCellValue(transaction.getReference());
                row.createCell(2).setCellValue(transaction.getCustomerPhoneNumber());
                row.createCell(3).setCellValue(transaction.getType().toString());
                row.createCell(4).setCellValue(transaction.getEarn().toString());
            }

            // Add total earnings row
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("Total Earnings:");
            totalRow.createCell(1).setCellValue(totalEarnings.toPlainString());

            // Add total deposits and withdrawals rows
            Row totalDepositRow = sheet.createRow(rowNum++);
            totalDepositRow.createCell(0).setCellValue("Total Deposits:");
            totalDepositRow.createCell(1).setCellValue(exportDataDto.getTotalDeposits().toPlainString());

            Row totalWithdrawalRow = sheet.createRow(rowNum++);
            totalWithdrawalRow.createCell(0).setCellValue("Total Withdrawals:");
            totalWithdrawalRow.createCell(1).setCellValue(exportDataDto.getTotalWithdrawals().toPlainString());

            Row netCashRow = sheet.createRow(rowNum++);
            netCashRow.createCell(0).setCellValue("Net Cash:");
            netCashRow.createCell(1).setCellValue(exportDataDto.getNetCash().toPlainString());


            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new GenericException("Error generating Excel file"+e.getMessage());
        }
    }


    public byte[] exportToPdf(LocalDateTime startDate, LocalDateTime endDate) {
        ExportDataDto exportDataDto = prepareExportData(startDate, endDate);
        List<ExportTransactionDto> transactions = exportDataDto.getTransactions();
        BigDecimal totalEarnings = exportDataDto.getTotalEarning();
        int transactionCount = transactions.size();

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Format date and time without milliseconds
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String reportGenerationTime = LocalDateTime.now().format(dateTimeFormatter); // Current report generation time
        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        String startDateFormatted = startDate.format(dateTimeFormatter2);
        String endDateFormatted = endDate.format(dateTimeFormatter2);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            // Add Title
            contentStream.beginText();
            contentStream.newLineAtOffset(150, 750); // Centered title
            contentStream.showText("Transaction Report for Agent : "+user.getName());
            contentStream.endText();

            // Define starting Y position for the table and column widths
            int yPosition = 700;
            int[] columnWidths = {70, 100, 120, 70, 50}; // Column widths for Amount, Reference, Phone, Type, Earn

            // Add Table Header
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            drawTableRow(contentStream, yPosition, columnWidths, new String[]{"Earn", "Reference", "Phone Number", "Type", "Amount"}, true);

            // Draw a line below the header
            yPosition -= 20;

            contentStream.setFont(PDType1Font.HELVETICA, 10);

            // Add Transaction Rows
            for (ExportTransactionDto transaction : transactions) {
                String[] rowData = new String[]{
                        transaction.getEarn() != null ? transaction.getEarn().toString() : "N/A",
                        transaction.getReference() != null ? transaction.getReference().toString() : "N/A",
                        transaction.getCustomerPhoneNumber() != null ? transaction.getCustomerPhoneNumber() : "N/A",
                        transaction.getType() != null ? transaction.getType().toString() : "N/A",
                        transaction.getAmount().toPlainString()
                };

                drawTableRow(contentStream, yPosition, columnWidths, rowData, false);
                yPosition -= 20;

                // Add a new page if the content exceeds the current page
                if (yPosition < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 700; // Reset Y position for the new page
                }
            }

            // Add Total Earnings under the Earn column
            yPosition -= 20;
            drawTableRow(contentStream, yPosition, columnWidths, new String[]{"Total Earning ", totalEarnings.toPlainString()}, false);

            yPosition -= 20;
            drawTableRow(contentStream, yPosition, columnWidths, new String[]{"Total Deposit ", exportDataDto.getTotalDeposits().toPlainString()}, false);

            yPosition -= 20;
            drawTableRow(contentStream, yPosition, columnWidths, new String[]{"Total Withdrawal ", exportDataDto.getTotalWithdrawals().toPlainString()}, false);

            yPosition -= 20;
            drawTableRow(contentStream, yPosition, columnWidths, new String[]{"Net Cash ", exportDataDto.getNetCash().toPlainString()}, false);

            // Number of transactions and report generation date at the bottom of the page
            contentStream.setFont(PDType1Font.HELVETICA, 8); // Small font size for additional details
            yPosition -= 40;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Number of Transactions: " + transactionCount);
            contentStream.newLineAtOffset(0, -10);
            contentStream.showText("Report from : "+startDateFormatted+" To "+endDateFormatted);
            contentStream.newLineAtOffset(0, -10);
            contentStream.showText("Report Generated on: " + reportGenerationTime);
            contentStream.endText();

            contentStream.close();
            document.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new GenericException("Error generating PDF file: " + e.getMessage());
        }
    }

    // Helper method to draw each row in the table
    private void drawTableRow(PDPageContentStream contentStream, int yPosition, int[] columnWidths, String[] rowData, boolean isHeader) throws IOException {
        int xPosition = 50;
        int height = 20;

        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setNonStrokingColor(isHeader ? Color.LIGHT_GRAY : Color.WHITE);

        // Draw cell background and borders
        for (int i = 0; i < columnWidths.length; i++) {
            contentStream.addRect(xPosition, yPosition, columnWidths[i], height);
            contentStream.fill();
            contentStream.moveTo(xPosition, yPosition);
            contentStream.lineTo(xPosition, yPosition + height);
            contentStream.stroke();
            xPosition += columnWidths[i];
        }

        // Draw cell borders
        xPosition = 50;
        contentStream.setNonStrokingColor(Color.BLACK); // Set font color to black
        for (int i = 0; i < columnWidths.length; i++) {
            contentStream.addRect(xPosition, yPosition, columnWidths[i], height);
            contentStream.stroke();
            xPosition += columnWidths[i];
        }

        // Add text into each cell
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, isHeader ? 10 : 8);
        xPosition = 50;
        for (int i = 0; i < rowData.length; i++) {
            contentStream.newLineAtOffset(xPosition + 5, yPosition + 5); // Slight padding for text
            contentStream.showText(rowData[i]);
            contentStream.endText();
            xPosition += columnWidths[i];
            contentStream.beginText();
        }
        contentStream.endText();
    }

    @Transactional
    public TransactionDto updateTransaction(Long transactionId, TransactionDto transactionDto) {
        // Find the existing transaction by ID
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new GenericException("Transaction not found"));

        // Check and update fields only if they are not null
        if (transactionDto.getAmount() != null) {
            existingTransaction.setAmount(transactionDto.getAmount());
        }
        if (transactionDto.getReference() != null) {
            existingTransaction.setReference(transactionDto.getReference());
        }
        if (transactionDto.getCustomerPhoneNumber() != null) {
            existingTransaction.setCustomerPhoneNumber(transactionDto.getCustomerPhoneNumber());
        }
        if (transactionDto.getType() != null) {
            existingTransaction.setType(transactionDto.getType());
        }
        if (transactionDto.getEarn() != null) {
            existingTransaction.setEarn(transactionDto.getEarn());
        }
        if (transactionDto.getNote() != null) {
            existingTransaction.setNote(transactionDto.getNote());
        }
        if (transactionDto.getCustomerOtp() != null) {
            existingTransaction.setCustomerOtp(transactionDto.getCustomerOtp());
        }

        // Set the update date
        existingTransaction.setUpdateDate(LocalDateTime.now());

        // Save the updated transaction
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        // Return the updated transaction as a DTO
        return transactionMapper.toTransactionDto(updatedTransaction);
    }

    public Page<TransactionDto> searchTransactions(String keyword, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)authentication.getPrincipal();
        log.info("ttt"+user.getAgency().getId());
        Long agencyId = user.getAgency().getId();
        return transactionMapper.toTransactionDtos(transactionRepository.searchTransactionsByKeyword(keyword, agencyId,pageable));
    }

}
