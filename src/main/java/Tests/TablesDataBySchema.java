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

public class TablesDataBySchema {
    String schema="";  //Set schema name
    String filename1= String.format("%sTablesData_%s%s.xlsx", Connections.filepath,schema, CommonMethods.getCurrentDateTime());

    Connection oracon=null;
    Connection pgcon=null;
    ResultSet ora_r=null;
    ResultSet pg_r=null;
    Statement ora_s=null;
    Statement pg_s=null;


    @BeforeClass
    void setUp() throws IOException, SQLException, ClassNotFoundException {
        System.setProperty("oracle.net.tns_admin", Connections.TNS_path);
        System.setProperty("oracle.jdbc.mapDateToTimestamp","false");
        String dbURL = Connections.OradbURL;
        Class.forName("oracle.jdbc.OracleDriver");

        oracon = DriverManager.getConnection(dbURL, Connections.OraUsername, Connections.OraPassword);

        pgcon = DriverManager.getConnection(Connections.PGdbURL, Connections.PGUsername, Connections.PGPassword);

        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Summary");
        Row row=sheet1.createRow(0);
        row.createCell(0).setCellValue("SCHEMA");
        row.createCell(1).setCellValue("TABLE");
        row.createCell(2).setCellValue("RESULT");
        row.createCell(3).setCellValue("Source Records");
        row.createCell(4).setCellValue("Target Records");
        row.createCell(5).setCellValue("Matched Records");
        row.createCell(6).setCellValue("SQL");
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
        ora_s.close();
        pg_s.close();

    }

    List<String> tables = new ArrayList<String>();

    int size=0;
    public void tableList(String schemaname) throws SQLException, ClassNotFoundException {


        String sqlq=String.format("select TABLE_NAME from All_tables where owner='%s' order by TABLE_NAME", schemaname);
        ora_s=oracon.createStatement();
        ora_r=ora_s.executeQuery(sqlq);

        while(ora_r.next()) {
            tables.add(ora_r.getString(1));
        }
        size=tables.size();


    }

    public String getTable(int t){
        return tables.get(t);
    }

    int index = 0,flag=0;
    @Test
    void test0() throws SQLException, IOException, ClassNotFoundException {
        tableList(schema);
        while (index < size) {

            Integer[] arr = new Integer[2];
            String columnNo1 = null;
            try {

                ResultSet o1 = oracon.createStatement().executeQuery(String.format("select count(*) from %s.%s",schema,getTable(index)));

                ResultSet p1 = pgcon.createStatement().executeQuery(String.format("select count(*) from %s.%s",schema,getTable(index)));
                o1.next();
                p1.next();
                arr[0]=o1.getInt(1);
                arr[1]=p1.getInt(1);

                o1.close();
                p1.close();

            } catch (PSQLException e) {
                System.out.println(e);
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(schema);
                row.createCell(1).setCellValue(getTable(index));
                row.createCell(2).setCellValue("FAIL");
                row.createCell(3).setCellValue(0);
                row.createCell(4).setCellValue(0);
                row.createCell(5).setCellValue(e.toString());
                flag = 1;
                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();
                index++;
                continue;
            }

            boolean colflag=false;

            if(!colflag){
                DatabaseMetaData metaData = oracon.getMetaData();
                ResultSet rs = metaData.getPrimaryKeys(Connections.OraCatalogName, schema, getTable(index));
                while(rs.next()) {
                    columnNo1 = rs.getString("COLUMN_NAME");
                    colflag = true;
                }
                rs.close();
            }


            if(!colflag){

                ResultSet o1=oracon.createStatement().executeQuery(String.format("select * from %s.%s",schema,getTable(index)));
                int j=1;

                if(!colflag){

                    int i=1;
                    while(i<o1.getMetaData().getColumnCount()){
                        ResultSet c = oracon.createStatement().executeQuery(String.format("select count(*) from (select %s, count(*) from %s.%s group by %s having count(*) > 1)",o1.getMetaData().getColumnLabel(i),schema,getTable(index),o1.getMetaData().getColumnLabel(i)));
                        c.next();
                        if(c.getString(1).equals("0")){
                            columnNo1=o1.getMetaData().getColumnLabel(i);
                            colflag=true;
                            c.close();
                            break;
                        }else{
                            i++;
                            if(i==o1.getMetaData().getColumnCount()-1)
                                c.close();
                        }

                    }

                }

                if(!colflag){
                    int i=1;
                    while (i < o1.getMetaData().getColumnCount()) {
                        if (!o1.getMetaData().getColumnTypeName(i).equalsIgnoreCase("CLOB")) {
                            columnNo1 = o1.getMetaData().getColumnLabel(i);
                            break;
                        } else i++;
                    }
                }
                o1.close();
            }


            String sqlq = String.format("Select * from %s.%s order by %s", schema, getTable(index), columnNo1);
            System.out.println(index+" "+sqlq);
            ora_s = oracon.createStatement();
            ora_r = ora_s.executeQuery(sqlq);

            pg_s = pgcon.createStatement();
            pg_r = pg_s.executeQuery(sqlq);

            Integer[] r = new Integer[2];

            r = CommonMethods.compareResultSets(ora_r, pg_r, filename1, getTable(index));


            if (r[1]==1) {
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(schema);
                row.createCell(1).setCellValue(getTable(index));
                row.createCell(2).setCellValue("PASS");
                row.createCell(3).setCellValue(arr[0]);
                row.createCell(4).setCellValue(arr[1]);
                row.createCell(5).setCellValue(r[0]);
                row.createCell(6).setCellValue(sqlq);
                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();

            } else {
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(schema);
                row.createCell(1).setCellValue(getTable(index));
                row.createCell(2).setCellValue("FAIL");
                row.createCell(3).setCellValue(arr[0]);
                row.createCell(4).setCellValue(arr[1]);
                row.createCell(5).setCellValue(r[0]);
                row.createCell(6).setCellValue(sqlq);

                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();
                flag = 1;

            }
            index++;

        }
        if (flag == 1) {
            Assert.assertEquals(false, true);
        } else Assert.assertEquals(true, true);


    }
}
