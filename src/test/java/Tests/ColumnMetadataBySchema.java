package Tests;

import Utils.CommonMethods;
import Utils.Connections;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.util.PSQLException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColumnMetadataBySchema {
    String schema="";  //Set Schema
    String filename1= String.format("%sColumnsMetadata%s%s.xlsx", Connections.filepath,schema, CommonMethods.getCurrentDateTime());
    static int exrow=1;
    Connection oracon=null;
    Connection pgcon=null;
    ResultSet ora_r=null;
    ResultSet pg_r=null;


    @BeforeClass
    void setUp() throws SQLException, ClassNotFoundException, IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Summary");
        Row row=sheet1.createRow(0);
        row.createCell(0).setCellValue("Tablename");
        row.createCell(1).setCellValue("Result");
        Sheet sheet2 = wb.createSheet("Details");
        Row row1=sheet2.createRow(0);
        row1.createCell(0).setCellValue("Tablename");
        row1.createCell(1).setCellValue("Column name");
        row1.createCell(2).setCellValue("Property");
        row1.createCell(3).setCellValue("Source");
        row1.createCell(4).setCellValue("Target");
        FileOutputStream fileOut = new FileOutputStream(filename1);
        wb.write(fileOut);
        fileOut.close();
    }

    @AfterClass
    void tearDown() throws SQLException {

        oracon.close();
        pgcon.close();
        ora_r.close();
        pg_r.close();

    }
    List<String> tables = new ArrayList<String>();
    int size=0;
    public void tableList( String schemaname) throws SQLException, ClassNotFoundException {
        System.setProperty("oracle.net.tns_admin", Connections.TNS_path);
        String dbURL = Connections.OradbURL;
        Class.forName("oracle.jdbc.OracleDriver");
        oracon = DriverManager.getConnection(dbURL, Connections.OraUsername, Connections.OraPassword);
        String sqlq=String.format("select TABLE_NAME from All_tables where owner='%s' order by TABLE_NAME", schemaname);
        ora_r=oracon.createStatement().executeQuery(sqlq);
        while(ora_r.next()) {
            tables.add(ora_r.getString(1));
        }
        size=tables.size();
        ora_r.close();
        oracon.close();

    }

    public String getTable(int t){
        return tables.get(t);
    }
    int index=0;
    @Test
    void test0() throws SQLException, IOException, ClassNotFoundException {

        tableList(schema);
        int flag=0;
        while(index<size) {

            Statement ora_s=null;
            Statement pg_s=null;
            String sqlq_o = String.format("select * from %s.%s where ROWNUM <= 10", schema, getTable(index));
            String sqlq_p = String.format("select * from %s.%s limit 10", schema, getTable(index));
            System.out.println(sqlq_p);
            ora_s = oracon.createStatement();
            ora_r=ora_s.executeQuery(sqlq_o);

            try{
                pg_s = pgcon.createStatement();
                pg_r=pg_s.executeQuery(sqlq_p);
            }catch(PSQLException e){
                System.out.println(e);
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(getTable(index));
                row.createCell(1).setCellValue("FAIL");
                row.createCell(2).setCellValue("Table missing in target");
                flag = 1;
                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();
                index++;
                continue;
            }

            int x = CommonMethods.compareColumnMeta(ora_r, pg_r, filename1, schema, getTable(index), exrow);

            ora_s.close();
            pg_s.close();

            if (x == exrow) {
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(getTable(index));
                row.createCell(1).setCellValue("PASS");

                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();

            } else {
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(getTable(index));
                row.createCell(1).setCellValue("FAIL");
                flag = 1;
                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();

            }
            exrow = x;
            index++;

        }
        if(flag==1){
            Assert.assertEquals(false,true);
        }
        else Assert.assertEquals(true,true);
    }
}
