// package com.emijusamuel.cashcove.service;

// import org.springframework.stereotype.Service;

// @Service
// public class ExcelService {

//     public void writeIncomesToExcel(OutputStream os, List<IncomeDTO> incomes){

//         try(Workbook workbook = new XSSFWorkbook()){
//             Sheet sheet = workbook.createSheet("Incomes");
//             Row header = sheet.createRow(0);
//             header.createCell(0).setCellValue("S.No");
//             header.createCell(1).setCellValue("Name");
//             header.createCell(2).setCellValue("Category");
//             header.createCell(3).setCellValue("Amount");
//             header.createCell(4).setCellValue("Date");
//             IntStream.range(0, incomes.size())
//             .forEach(i -> {
//                 IncomeDTO income = incomes.get(i);
//                 Row row = sheet.createRow(i + 1);
//                 row.createCell(0).setCellValue(i + 1);
//                 row.createCell(1).setCellValue(income.getName() != null ? income.getName(): "N/A");
//                 row.createCell(1).setCellValue(income.getName() != null ? income.getName(): "N/A");
//             })
//         }

//     }
// }
