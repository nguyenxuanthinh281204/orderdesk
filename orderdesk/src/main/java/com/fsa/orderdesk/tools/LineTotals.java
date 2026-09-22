package com.fsa.orderdesk.tools;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LineTotals {

    // Record mang dữ liệu kết quả thuần túy
    public record Summary(long lines, long units, BigDecimal total) {
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: Missing input file argument.");
            System.exit(1);
        }

        try {
            List<String> rawLines = Files.readAllLines(Path.of(args[0]));
            Summary summary = calculate(rawLines);

            System.out.println("lines: " + summary.lines());
            System.out.println("units: " + summary.units());
            System.out.println("total: " + summary.total().toPlainString());
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // Logic thuần túy: Tách biệt hoàn toàn khỏi I/O để test độc lập
    static Summary calculate(List<String> fileLines) {
        if (fileLines == null || fileLines.isEmpty()) {
            return new Summary(0, 0, BigDecimal.ZERO);
        }

        // Bỏ dòng tiêu đề (header row)
        List<String> dataRows = fileLines.subList(1, fileLines.size());
        if (dataRows.isEmpty()) {
            return new Summary(0, 0, BigDecimal.ZERO);
        }

        long linesCount = 0;
        long unitsCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < dataRows.size(); i++) {
            String rawRow = dataRows.get(i);
            int lineNumber = i + 2; // Dòng 1 là header, nên dòng data đầu tiên là dòng số 2

            // Dùng limit = -1 để không làm mất các trường rỗng cuối dòng
            String[] fields = rawRow.split(",", -1);

            if (fields.length < 3) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": Row must have at least 3 fields, got " + fields.length);
            }

            String sku = fields[0].trim();
            String qtyStr = fields[1].trim();
            String priceStr = fields[2].trim();

            if (sku.isEmpty()) {
                throw new IllegalArgumentException("Line " + lineNumber + ": SKU cannot be empty");
            }

            // Kiểm tra Quantity: Phải là số nguyên dương (> 0)
            long quantity;
            try {
                quantity = Long.parseLong(qtyStr);
                if (quantity <= 0) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + ": Quantity must be a positive integer, got: " + qtyStr);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": Invalid quantity integer format: '" + qtyStr + "'");
            }

            // Kiểm tra Unit Price: Không được âm, parse bằng String constructor
            BigDecimal unitPrice;
            try {
                unitPrice = new BigDecimal(priceStr);
                if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + ": Unit price cannot be negative, got: " + priceStr);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Line " + lineNumber + ": Invalid unit price format: '" + priceStr + "'");
            }

            linesCount++;
            unitsCount += quantity;
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return new Summary(linesCount, unitsCount, totalAmount);
    }
}
