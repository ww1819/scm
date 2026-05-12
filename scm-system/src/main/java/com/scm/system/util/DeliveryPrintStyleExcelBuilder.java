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
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
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
import com.scm.common.core.text.Convert;
import com.scm.common.utils.StringUtils;
import com.scm.system.domain.Delivery;
import com.scm.system.domain.DeliveryDetail;
import com.scm.system.domain.vo.DeliveryPrintSheetVo;

/**
 * 将配送单按「物资配送单」打印版式写入 Excel（每单一个 Sheet）。
 * <p>
 * 版式与边框约定：信息区、页脚数值区为「仅底边框」下划线样式；明细表头至合计行为全框线；
 * 标题行无表框；不生成条码图片（仅保留「输入码」文字）。
 */
public final class DeliveryPrintStyleExcelBuilder
{
    private static final int COLS = 12;

    private DeliveryPrintStyleExcelBuilder()
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
        String base = StringUtils.isNotEmpty(deliveryNo) ? deliveryNo : ("配送单" + idx);
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
        Delivery d = vo.getDelivery();
        if (d == null)
        {
            d = new Delivery();
        }
        String code = StringUtils.trimToEmpty(vo.getPrintInputCode());

        for (int c = 0; c < COLS; c++)
        {
            sh.setColumnWidth(c, 12 * 256);
        }
        sh.setColumnWidth(1, 18 * 256);
        sh.setColumnWidth(2, 22 * 256);
        sh.setColumnWidth(11, 20 * 256);

        int r = 0;
        Row r0 = sh.createRow(r++);
        r0.setHeightInPoints(26);
        merge(sh, 0, 0, 0, 3);
        setStr(sh, 0, 0, "", st.plain);
        merge(sh, 0, 0, 4, 7);
        setStr(sh, 0, 4, "物资配送单", st.title);
        merge(sh, 0, 0, 8, 11);
        setStr(sh, 0, 8, "输入码：" + code, st.inputCode);

        r = metaLine(sh, r, "配送商：", nz(d.getSupplierName()), "医院：", nz(d.getHospitalName()), "配送单号：", nz(d.getDeliveryNo()), st);
        r = metaLine(sh, r, "配送地址：", nz(d.getDeliveryAddress()), "供应商：", nz(d.getSupplierName()), "仓库：", nz(d.getWarehouse()), st);
        r = metaLine(sh, r, "金额：", fmtMoney(vo.getPrintTotalAmount()), "发票：", nz(d.getInvoiceNo()), "备注：", nz(d.getRemark()), st);

        sh.createRow(r++);

        String[] heads = { "序号", "品名", "规格", "单位", "单价", "数量", "金额", "批号", "有效期", "生产日期", "注册证号", "厂家" };
        int hr = r;
        sh.createRow(hr);
        for (int c = 0; c < COLS; c++)
        {
            setStr(sh, hr, c, heads[c], st.header);
        }
        r++;

        List<DeliveryDetail> details = vo.getDeliveryDetails();
        int seq = 1;
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
                setStr(sh, rr, col++, String.valueOf(seq++), st.border);
                setStr(sh, rr, col++, nz(det.getMaterialName()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getSpecification()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getUnit()), st.borderLeft);
                setNum(sh, rr, col++, det.getPrice(), st.borderNum);
                setNumInt(sh, rr, col++, det.getDeliveryQuantity(), st.borderNumInt);
                setNum(sh, rr, col++, det.getAmount(), st.borderNum);
                setStr(sh, rr, col++, nz(det.getBatchNo()), st.borderLeft);
                setDate(sh, rr, col++, det.getExpireDate(), st.date, st.borderLeft);
                setDate(sh, rr, col++, det.getProductionDate(), st.date, st.borderLeft);
                setStr(sh, rr, col++, nz(det.getRegisterNo()), st.borderLeft);
                setStr(sh, rr, col++, nz(det.getManufacturer()), st.borderLeft);
            }
        }

        int sr = r++;
        sh.createRow(sr);
        merge(sh, sr, sr, 0, 3);
        setStr(sh, sr, 0, "合计", st.borderBold);
        setStr(sh, sr, 4, "", st.border);
        setNumInt(sh, sr, 5, BigDecimal.valueOf(vo.getTotalQuantity()), st.borderNumInt);
        setNum(sh, sr, 6, vo.getPrintTotalAmount(), st.borderNum);
        merge(sh, sr, sr, 7, 11);
        String upper = vo.getPrintTotalAmount() != null
            ? Convert.digitUppercase(vo.getPrintTotalAmount().setScale(2, RoundingMode.HALF_UP).doubleValue())
            : "零元整";
        setStr(sh, sr, 7, "合计大写金额：" + upper, st.borderLeft);

        int fr = r++;
        Row footRow = sh.createRow(fr);
        footRow.setHeightInPoints(20);
        merge(sh, fr, fr, 0, 2);
        setStr(sh, fr, 0, "制单日期：" + fmtDate(d.getCreateTime()), st.footer);
        merge(sh, fr, fr, 3, 5);
        setStr(sh, fr, 3, "制单人：" + nz(d.getCreateBy()), st.footer);
        merge(sh, fr, fr, 6, 8);
        setStr(sh, fr, 6, "签收人：______________________________", st.footer);
        merge(sh, fr, fr, 9, 11);
        setStr(sh, fr, 9, "签收日期：______________________________", st.footer);
    }

    private static int metaLine(Sheet sh, int r, String l1, String v1, String l2, String v2, String l3, String v3, Styles st)
    {
        Row row = sh.createRow(r);
        row.setHeightInPoints(18);
        setStr(sh, r, 0, l1, st.metaLabel);
        merge(sh, r, r, 1, 3);
        setStr(sh, r, 1, v1, st.metaValue);
        setStr(sh, r, 4, l2, st.metaLabel);
        merge(sh, r, r, 5, 7);
        setStr(sh, r, 5, v2, st.metaValue);
        setStr(sh, r, 8, l3, st.metaLabel);
        merge(sh, r, r, 9, 11);
        setStr(sh, r, 9, v3, st.metaValue);
        return r + 1;
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

    private static void setNum(Sheet sh, int r, int c, BigDecimal v, CellStyle style)
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
        if (v == null)
        {
            cell.setCellValue("");
        }
        else
        {
            cell.setCellValue(v.doubleValue());
        }
        if (style != null)
        {
            cell.setCellStyle(style);
        }
    }

    private static void setNumInt(Sheet sh, int r, int c, BigDecimal v, CellStyle style)
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
        if (v == null)
        {
            cell.setCellValue("");
        }
        else
        {
            cell.setCellValue(v.intValue());
        }
        if (style != null)
        {
            cell.setCellStyle(style);
        }
    }

    private static void setDate(Sheet sh, int r, int c, Date v, CellStyle dateStyle, CellStyle fallback)
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
        if (v == null)
        {
            cell.setCellValue("-");
            cell.setCellStyle(fallback);
        }
        else
        {
            cell.setCellValue(v);
            cell.setCellStyle(dateStyle);
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
            return "";
        }
        return new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA).format(d);
    }

    private static final class Styles
    {
        /** 信息区标签：无框线、加粗 */
        final CellStyle metaLabel;
        /** 信息区取值：仅底边框（下划线） */
        final CellStyle metaValue;
        /** 标题行左侧留白 */
        final CellStyle plain;
        /** 主标题 */
        final CellStyle title;
        /** 输入码：无框、右对齐 */
        final CellStyle inputCode;
        /** 表头：灰底全框 */
        final CellStyle header;
        /** 明细/合计：全框 */
        final CellStyle border;
        final CellStyle borderLeft;
        final CellStyle borderBold;
        final CellStyle borderNum;
        final CellStyle borderNumInt;
        final CellStyle date;
        /** 页脚：仅底边框 */
        final CellStyle footer;

        Styles(Workbook wb)
        {
            CreationHelper ch = wb.getCreationHelper();
            DataFormat df = ch.createDataFormat();

            metaLabel = wb.createCellStyle();
            metaLabel.setVerticalAlignment(VerticalAlignment.CENTER);
            metaLabel.setWrapText(true);
            Font fl = wb.createFont();
            fl.setBold(true);
            metaLabel.setFont(fl);
            clearBorders(metaLabel);

            metaValue = wb.createCellStyle();
            metaValue.setVerticalAlignment(VerticalAlignment.CENTER);
            metaValue.setWrapText(true);
            metaValue.setAlignment(HorizontalAlignment.LEFT);
            clearBorders(metaValue);
            metaValue.setBorderBottom(BorderStyle.THIN);

            plain = wb.createCellStyle();
            clearBorders(plain);

            title = wb.createCellStyle();
            title.setAlignment(HorizontalAlignment.CENTER);
            title.setVerticalAlignment(VerticalAlignment.CENTER);
            clearBorders(title);
            Font ft = wb.createFont();
            ft.setBold(true);
            ft.setFontHeightInPoints((short) 14);
            title.setFont(ft);

            inputCode = wb.createCellStyle();
            inputCode.setAlignment(HorizontalAlignment.RIGHT);
            inputCode.setVerticalAlignment(VerticalAlignment.CENTER);
            clearBorders(inputCode);

            header = thinAll(wb);
            header.setAlignment(HorizontalAlignment.CENTER);
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

            borderBold = thinAll(wb);
            borderBold.setVerticalAlignment(VerticalAlignment.CENTER);
            Font fb = wb.createFont();
            fb.setBold(true);
            borderBold.setFont(fb);

            borderNum = thinAll(wb);
            borderNum.setVerticalAlignment(VerticalAlignment.CENTER);
            borderNum.setAlignment(HorizontalAlignment.RIGHT);
            borderNum.setDataFormat(df.getFormat("#,##0.00"));

            borderNumInt = thinAll(wb);
            borderNumInt.setVerticalAlignment(VerticalAlignment.CENTER);
            borderNumInt.setAlignment(HorizontalAlignment.RIGHT);
            borderNumInt.setDataFormat(df.getFormat("#,##0"));

            date = thinAll(wb);
            date.setVerticalAlignment(VerticalAlignment.CENTER);
            date.setAlignment(HorizontalAlignment.CENTER);
            date.setDataFormat(df.getFormat("yyyy/mm/dd"));

            footer = wb.createCellStyle();
            footer.setVerticalAlignment(VerticalAlignment.CENTER);
            footer.setWrapText(true);
            clearBorders(footer);
            footer.setBorderBottom(BorderStyle.THIN);
        }

        private static void clearBorders(CellStyle s)
        {
            s.setBorderTop(BorderStyle.NONE);
            s.setBorderBottom(BorderStyle.NONE);
            s.setBorderLeft(BorderStyle.NONE);
            s.setBorderRight(BorderStyle.NONE);
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
