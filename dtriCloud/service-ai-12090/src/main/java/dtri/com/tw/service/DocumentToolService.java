package dtri.com.tw.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class DocumentToolService {

	// 1. 🚀 PDF 轉圖片 (解決 vLLM 不支援 document 標籤的問題)
	public static List<byte[]> pdfToImages(File file) throws IOException {
		List<byte[]> images = new ArrayList<>();
		try (PDDocument document = PDDocument.load(file)) {
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			// 只抓前 3 頁防止記憶體爆炸，產線報表通常前幾頁就是重點
			int pages = Math.min(document.getNumberOfPages(), 3);
			for (int i = 0; i < pages; i++) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, org.apache.pdfbox.rendering.ImageType.RGB);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bim, "jpg", baos);
				images.add(baos.toByteArray());
			}
		}
		return images;
	}

	// 2. 🚀 PDF 轉 JSON 文字 (提取純文字邏輯)
	public static String pdfToJson(File file) throws IOException {
		JsonObject result = new JsonObject();
		try (PDDocument document = PDDocument.load(file)) {
			PDFTextStripper stripper = new PDFTextStripper();
			result.addProperty("fileName", file.getName());
			result.addProperty("content", stripper.getText(document).trim());
		}
		return result.toString();
	}

	// 3. 🚀 Word 轉 JSON 文字 (支援 .docx 與 .doc)
	public static String wordToJson(File file) throws IOException {
		JsonObject result = new JsonObject();
		JsonArray paragraphs = new JsonArray();
		String fileName = file.getName().toLowerCase();

		try (FileInputStream fis = new FileInputStream(file)) {
			if (fileName.endsWith(".docx")) {
				// --- 處理新版 .docx (OOXML) ---
				try (XWPFDocument document = new XWPFDocument(fis)) {
					document.getParagraphs().forEach(p -> {
						if (!p.getText().isBlank())
							paragraphs.add(p.getText().trim());
					});
				}
			} else if (fileName.endsWith(".doc")) {
				// --- 處理舊版 .doc (OLE2) ---
				try (HWPFDocument document = new HWPFDocument(fis)) {
					WordExtractor extractor = new WordExtractor(document);
					String[] textArray = extractor.getParagraphText();
					for (String p : textArray) {
						if (p != null && !p.isBlank())
							paragraphs.add(p.trim());
					}
				}
			} else {
				throw new IOException("不支援的 Word 格式: " + fileName);
			}

			// 保持你原本的回傳格式
			result.addProperty("type", "Word Document");
			result.add("data", paragraphs);

		} catch (Exception e) {
			throw new IOException("解析 Word 失敗: " + e.getMessage(), e);
		}

		return result.toString();
	}

	// 4. 🚀 Excel 轉 JSON 文字 (支援 .xlsx 與 .xls)
	public static String excelToJson(File file) throws IOException {
	    JsonObject result = new JsonObject();
	    JsonArray sheetsArray = new JsonArray();
	    
	    // 🚀 優化 1：將 Formatter 移出迴圈，重複使用
	    DataFormatter formatter = new DataFormatter();

	    try (FileInputStream fis = new FileInputStream(file); 
	         Workbook workbook = WorkbookFactory.create(fis)) { // Factory 會自動處理 xls/xlsx

	        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
	            Sheet sheet = workbook.getSheetAt(i);
	            JsonObject sheetJson = new JsonObject();
	            JsonArray rowsArray = new JsonArray();

	            for (Row row : sheet) {
	                JsonArray rowData = new JsonArray();
	                // 🚀 優化 2：遍歷所有單元格，包含空值
	                // 使用 getLastCellNum 確保每一列的長度一致，對 AI 閱讀更有利
	                short lastColumn = row.getLastCellNum();
	                for (int cn = 0; cn < lastColumn; cn++) {
	                    Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
	                    rowData.add(formatter.formatCellValue(cell).trim());
	                }
	                rowsArray.add(rowData);
	            }
	            sheetJson.addProperty("sheetName", sheet.getSheetName());
	            sheetJson.add("rows", rowsArray);
	            sheetsArray.add(sheetJson);
	        }
	        
	        // 🚀 優化 3：保持與 Word 格式一致的標籤
	        result.addProperty("type", "Excel Document");
	        result.add("sheets", sheetsArray);
	    } catch (Exception e) {
	        throw new IOException("Excel 解析失敗: " + e.getMessage(), e);
	    }
	    return result.toString();
	}
}
