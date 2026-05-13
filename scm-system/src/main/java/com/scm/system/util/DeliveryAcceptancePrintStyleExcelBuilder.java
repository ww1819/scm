package com.scm.system.util;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.vo.DeliveryPrintSheetVo;

/**
 * 医用耗材质量验收单：按 {@code printAcceptance.html} 版式写入 Excel（每单一个 Sheet）。
 */
public final class DeliveryAcceptancePrintStyleExcelBuilder
{
    private static final int COLS = 13;

    private DeliveryAcceptancePrintStyleExcelBuilder()
    {
    }

    public static void write(List<DeliveryPrintSheetVo> sheets, OutputStream out) throws IOException
    {
        if (sheets == null || sheets.isEmpty())
        {
            throw new IllegalArgumentException("sheets empty");
        }
        Workbook wb = new XSSFWorkbook();
        try
        {
            Styles st = new Styles(wb);
            Set<String> usedNames = new HashSet<>();
            for (int i = 0; i < sheets.size(); i++)
            {
                DeliveryPrintSheetVo vo = sheets.get(i);
                String base = vo.getDelivery() != null ? vo.getDelivery().getDeliveryNo() : null;
                String sheetName = uniqueSheetName(base, i + 1, usedNames);
                Sheet sh = wb.createSheet(sheetName);
                fillSheet(sh, vo, st);
            }
            wb.write(out);
        }
        finally
        {
            wb.close();
        }
    }

    private static String uniqueSheetName(String deliveryNo, int idx, Set<String> used)
    {
        String base = StringUtils.isNotEmpty(deliveryNo) ? deliveryNo : ("验收单" + idx);
        String s = base.replaceAll("[\\\\/*?:\\[\\]]", "_");
        if (s.length() > 31)
        {
            s = s.substring(0, 31);
        }
        String name = s;
        int n = 2;
        while (!used.add(name))
        {
            String suffix = "(" + n + ")";
            int max = 31 - suffix.length();
            String p = s.length() > max ? s.substring(0, Math.max(1, max)) : s;
            name = (p + suffix).substring(0, Math.min(31, (p + suffix).length()));
            n++;
        }
        return name;
    }

    private static void fillSheet(Sheet sh, DeliveryPrintSheetVo vo, Styles st)
    {
        Delivery d = vo.getDelivery() != null ? vo.getDelivery() : new Delivery();
        int[] colW = { 12, 12, 6, 8, 7, 14, 10, 10, 10, 12, 8, 12, 12 };
        for (int c = 0; c < COLS; c++)
        {
            sh.setColumnWidth(c, colW[c] * 256);
        }

        int r = 0;
        String hospital = StringUtils.trimToEmpty(d.getHospitalName());
        if (StringUtils.isEmpty(hospital))
        {
            hospital = "—";
        }
        Row titleRow = sh.createRow(r++);
        merge(sh, 0, 0, 0, COLS - 1);
        String titleText = hospital + "医用耗材质量验收单";
        setStr(sh, 0, 0, titleText, st.title);
        setTitleRowHeight(sh, titleRow, titleText);

        Row m1 = sh.createRow(r++);
        String m1t1 = "配送商：" + nz(d.getSupplierName());
        String m1t2 = "订单号：" + nz(d.getOrderNo());
        String m1t3 = "仓库：" + nz(d.getWarehouse());
        String m1t4 = "配送单号：" + nz(d.getDeliveryNo());
        setMetaQuad(sh, m1.getRowNum(), m1t1, m1t2, m1t3, m1t4, st);
        setMetaQuadRowHeight(sh, m1, m1t1, m1t2, m1t3, m1t4);

        Row m2 = sh.createRow(r++);
        String m2t1 = "供应商：" + nz(d.getSupplierName());
        String m2t2 = "医院：" + nz(d.getHospitalName());
        String m2t3 = "总金额：" + fmtMoney(vo.getPrintTotalAmount());
        String m2t4 = "发票号：" + nz(d.getInvoiceNo());
        setMetaQuad(sh, m2.getRowNum(), m2t1, m2t2, m2t3, m2t4, st);
        setMetaQuadRowHeight(sh, m2, m2t1, m2t2, m2t3, m2t4);

        sh.createRow(r++);

        String[] heads = { "名称", "规格", "单位", "单价", "数量", "厂家", "批号", "生产日期", "有效期", "注册证号", "合格证", "生产企业许可证", "质量情况" };
        int hr = r++;
        Row hrow = sh.createRow(hr);
        hrow.setHeightInPoints(20);
        for (int c = 0; c < COLS; c++)
        {
            setStr(sh, hr, c, heads[c], st.header);
        }

        List<DeliveryDetail> details = vo.getDeliveryDetails();
        if (details != null)
        {
            for (DeliveryDetail det : details)
            {
                if (det == null)
                {
                    continue;
                }
                Row dr = sh.createRow(r++);
                int rr = dr.getRowNum();
                int col = 0;
                setStr(sh, rr, col++, nz(det.getMaterialName()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getSpecification()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getUnit()), st.border);
                setStr(sh, rr, col++, det.getPrice() != null ? det.getPrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : "-", st.border);
                setStr(sh, rr, col++, det.getDeliveryQuantity() != null ? det.getDeliveryQuantity().setScale(0, RoundingMode.HALF_UP).toPlainString() : "-", st.border);
                setStr(sh, rr, col++, nz(det.getManufacturer()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getBatchNo()), st.border);
                setStr(sh, rr, col++, fmtDate(det.getProductionDate()), st.border);
                setStr(sh, rr, col++, fmtDate(det.getExpireDate()), st.border);
                setStr(sh, rr, col++, nz(det.getRegisterNo()), st.borderLeft);
                setStr(sh, rr, col++, "", st.border);
                setStr(sh, rr, col++, "", st.border);
                setStr(sh, rr, col++, "", st.border);
            }
        }

        if (details == null || details.isEmpty())
        {
            Row er = sh.createRow(r++);
            merge(sh, er.getRowNum(), er.getRowNum(), 0, COLS - 1);
            setStr(sh, er.getRowNum(), 0, "暂无明细数据", st.border);
        }

        r++;
        Row s1 = sh.createRow(r++);
        s1.setHeightInPoints(20);
        merge(sh, s1.getRowNum(), s1.getRowNum(), 0, 4);
        setStr(sh, s1.getRowNum(), 0, "物资材料供应处：______________________________", st.signLine);
        merge(sh, s1.getRowNum(), s1.getRowNum(), 5, 8);
        setStr(sh, s1.getRowNum(), 5, "科室签收人：______________________________", st.signLine);
        String deliverer = StringUtils.trimToEmpty(d.getDeliveryPersonForDisplay());
        merge(sh, s1.getRowNum(), s1.getRowNum(), 9, 12);
        setStr(sh, s1.getRowNum(), 9, "送货人：" + (StringUtils.isNotEmpty(deliverer) ? deliverer + " " : "") + "______________", st.signLine);

        Row s2 = sh.createRow(r++);
        s2.setHeightInPoints(20);
        merge(sh, s2.getRowNum(), s2.getRowNum(), 0, 4);
        setStr(sh, s2.getRowNum(), 0, "配送日期：" + fmtDate(d.getExpectedDeliveryDate()), st.signLine);
        merge(sh, s2.getRowNum(), s2.getRowNum(), 5, 12);
        setStr(sh, s2.getRowNum(), 5, "经手人签字：______________________________", st.signLine);
    }

    /** 一行四块信息，每块占 3～4 列合并 */
    private static void setMetaQuad(Sheet sh, int rowIdx, String t1, String t2, String t3, String t4, Styles st)
    {
        merge(sh, rowIdx, rowIdx, 0, 2);
        setStr(sh, rowIdx, 0, t1, st.metaCell);
        merge(sh, rowIdx, rowIdx, 3, 5);
        setStr(sh, rowIdx, 3, t2, st.metaCell);
        merge(sh, rowIdx, rowIdx, 6, 8);
        setStr(sh, rowIdx, 6, t3, st.metaCell);
        merge(sh, rowIdx, rowIdx, 9, 12);
        setStr(sh, rowIdx, 9, t4, st.metaCell);
    }

    /** 合并区列宽折算为「等效拉丁字符」宽度，用于估算换行（中文按约 2 宽计）。 */
    private static float mergedCharsWide(Sheet sh, int c1, int c2)
    {
        double w = 0;
        for (int c = c1; c <= c2; c++)
        {
            w += sh.getColumnWidth(c) / 256.0;
        }
        return (float) Math.max(4.0, w * 0.9);
    }

    private static int effectiveCharUnits(String s)
    {
        if (s == null || s.isEmpty())
        {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < s.length(); )
        {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            n += (cp > 127) ? 2 : 1;
        }
        return n;
    }

    private static int estimateWrappedLines(String text, float charsPerLine)
    {
        if (text == null || text.isEmpty())
        {
            return 1;
        }
        int maxLines = 1;
        for (String part : text.split("\n", -1))
        {
            int eff = effectiveCharUnits(part);
            float cpl = Math.max(4f, charsPerLine);
            maxLines = Math.max(maxLines, (int) Math.ceil(eff / cpl));
        }
        return maxLines;
    }

    private static void setTitleRowHeight(Sheet sh, Row row, String text)
    {
        int lines = estimateWrappedLines(text, mergedCharsWide(sh, 0, COLS - 1));
        float pt = Math.max(28f, Math.min(200f, 10f + lines * 16f));
        row.setHeightInPoints(pt);
    }

    private static void setMetaQuadRowHeight(Sheet sh, Row row, String t1, String t2, String t3, String t4)
    {
        int l1 = estimateWrappedLines(t1, mergedCharsWide(sh, 0, 2));
        int l2 = estimateWrappedLines(t2, mergedCharsWide(sh, 3, 5));
        int l3 = estimateWrappedLines(t3, mergedCharsWide(sh, 6, 8));
        int l4 = estimateWrappedLines(t4, mergedCharsWide(sh, 9, 12));
        int maxL = Math.max(Math.max(l1, l2), Math.max(l3, l4));
        float pt = Math.max(18f, Math.min(180f, 8f + maxL * 14f));
        row.setHeightInPoints(pt);
    }

    private static void merge(Sheet sh, int r1, int r2, int c1, int c2)
    {
        if (r1 == r2 && c1 == c2)
        {
            return;
        }
        sh.addMergedRegion(new CellRangeAddress(r1, r2, c1, c2));
    }

    private static void setStr(Sheet sh, int r, int c, String v, CellStyle style)
    {
        Row row = sh.getRow(r);
        if (row == null)
        {
            row = sh.createRow(r);
        }
        Cell cell = row.getCell(c);
        if (cell == null)
        {
            cell = row.createCell(c);
        }
        cell.setCellValue(v == null ? "" : v);
        if (style != null)
        {
            cell.setCellStyle(style);
        }
    }

    private static String nz(String s)
    {
        return StringUtils.isNotEmpty(s) ? s : "-";
    }

    private static String fmtMoney(BigDecimal a)
    {
        if (a == null)
        {
            return "0.00";
        }
        return a.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String fmtDate(Date d)
    {
        if (d == null)
        {
            return "-";
        }
        return new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(d);
    }

    private static final class Styles
    {
        final CellStyle title;
        final CellStyle metaCell;
        final CellStyle header;
        final CellStyle border;
        final CellStyle borderLeft;
        final CellStyle signLine;

        Styles(Workbook wb)
        {
            title = noBorder(wb);
            title.setAlignment(HorizontalAlignment.CENTER);
            title.setVerticalAlignment(VerticalAlignment.CENTER);
            title.setWrapText(true);
            Font ft = wb.createFont();
            ft.setBold(true);
            ft.setFontHeightInPoints((short) 14);
            title.setFont(ft);

            metaCell = noBorder(wb);
            metaCell.setAlignment(HorizontalAlignment.LEFT);
            metaCell.setVerticalAlignment(VerticalAlignment.CENTER);
            metaCell.setWrapText(true);

            header = thinAll(wb);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            header.setWrapText(true);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font fh = wb.createFont();
            fh.setBold(true);
            header.setFont(fh);

            border = thinAll(wb);
            border.setVerticalAlignment(VerticalAlignment.CENTER);
            border.setWrapText(true);

            borderLeft = thinAll(wb);
            borderLeft.setVerticalAlignment(VerticalAlignment.CENTER);
            borderLeft.setWrapText(true);
            borderLeft.setAlignment(HorizontalAlignment.LEFT);

            signLine = noBorder(wb);
            signLine.setVerticalAlignment(VerticalAlignment.CENTER);
            signLine.setWrapText(true);
            signLine.setAlignment(HorizontalAlignment.LEFT);
        }

        private static CellStyle noBorder(Workbook wb)
        {
            CellStyle s = wb.createCellStyle();
            s.setBorderTop(BorderStyle.NONE);
            s.setBorderBottom(BorderStyle.NONE);
            s.setBorderLeft(BorderStyle.NONE);
            s.setBorderRight(BorderStyle.NONE);
            return s;
        }

        private static CellStyle thinAll(Workbook wb)
        {
            CellStyle s = wb.createCellStyle();
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            return s;
        }
    }
}
