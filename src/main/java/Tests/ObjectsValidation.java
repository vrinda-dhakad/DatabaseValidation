package Tests;

import Utils.CommonMethods;
import Utils.Connections;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectsValidation {
    String filename1= String.format("%sObjects_Validation_%s.xlsx", Connections.filepath,CommonMethods.getCurrentDateTime());

    Connection oracon=null;
    Connection pgcon=null;
    ResultSet ora_r=null;
    ResultSet pg_r=null;

    @BeforeClass
    void setUp() throws SQLException, ClassNotFoundException, IOException {
        System.setProperty("oracle.net.tns_admin", Connections.TNS_path);
        String dbURL = Connections.OradbURL;
        Class.forName("oracle.jdbc.OracleDriver");
        oracon = DriverManager.getConnection(dbURL, Connections.OraUsername, Connections.OraPassword);

        pgcon = DriverManager.getConnection(Connections.PGdbURL, Connections.PGUsername, Connections.PGPassword);


        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Summary");
        Row row=sheet1.createRow(0);
        row.createCell(0).setCellValue("OBJECT");
        row.createCell(1).setCellValue("RESULT");
        row.createCell(2).setCellValue("TOTAL");
        row.createCell(3).setCellValue("MISSING");
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

    @Test(priority=1)
    public void validateSchemas() throws SQLException, IOException {
        int index=0;
        String object = "Schemas";
        ora_r = oracon.createStatement().executeQuery("select distinct owner from all_tables ORDER BY owner");
        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select distinct schemaname as table_schema from pg_tables where schemaname not in('information_schema','pg_catalog') group by schemaname order by schemaname");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }

        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }


    @Test(priority=2)
    public void validateTables() throws SQLException, IOException {
        int index=1;
        String object = "Tables";
        ora_r = oracon.createStatement().executeQuery("select owner || '.' || table_name  from all_tables ORDER BY owner, table_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select schemaname || '.' || tablename from pg_tables where schemaname not in('information_schema','pg_catalog') order by schemaname, tablename");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }
        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }

    @Test(priority=3)
    public void validateViews() throws SQLException, IOException {
        int index=2;
        String object = "Views";
        ora_r = oracon.createStatement().executeQuery("select owner || '.' || view_name from all_views ORDER BY owner, view_name");
        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("SELECT schemaname|| '.' || viewname FROM pg_views WHERE schemaname NOT IN ('pg_catalog', 'information_schema')AND viewname !~ '^pg_' order by schemaname, viewname");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }

        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }


    }

    @Test(priority=4)
    public void validateIndex() throws SQLException, IOException {
        int index=3;
        String object = "Indexes";
        ora_r = oracon.createStatement().executeQuery("select owner || '.' || index_name  from all_indexes ORDER BY owner, INDEx_name");
        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select schemaname || '.' || indexname from pg_indexes where schemaname not in('information_schema','pg_catalog') order by schemaname, indexname");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }
        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }


    }

    @Test(priority=5)
    public void validateProcedures() throws SQLException, IOException {
        int index=4;
        String object = "Procedures";
        ora_r = oracon.createStatement().executeQuery("SELECT Procedure_name FROM ALL_PROCEDURES WHERE OBJECT_TYPE='PACKAGE' AND PROCEDURE_NAME IS NOT NULL ORDER BY Procedure_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select routine_name from information_schema.routines where routine_type = 'PROCEDURE' and specific_schema not in('information_schema','pg_catalog') order by routine_name");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }
        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }

    @Test(priority=6)
    public void validateFunctions() throws SQLException, IOException {
        int index=5;
        String object = "Functions";
        ora_r = oracon.createStatement().executeQuery("SELECT owner || '.' || Object_name FROM ALL_PROCEDURES WHERE OBJECT_TYPE='FUNCTION' ORDER BY owner, Object_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select specific_schema || '.' || routine_name from information_schema.routines where routine_type = 'FUNCTION' and specific_schema not in('information_schema','pg_catalog') order by specific_schema, routine_name");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }
        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }

    @Test(priority=7)
    public void validateTriggers() throws SQLException, IOException {
        int index=6;
        String object = "Triggers";
        ora_r = oracon.createStatement().executeQuery("select owner || '.' || TRIGGER_NAME from all_triggers ORDER BY owner, trigger_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select trigger_schema || '.' || trigger_name from INFORMATION_SCHEMA.triggers where trigger_schema not in('information_schema','pg_catalog') order by trigger_schema, trigger_name");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }
        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }


    }

    @Test(priority=8)
    public void validateSequences() throws SQLException, IOException {
        int index=7;
        String object = "Sequences";
        ora_r = oracon.createStatement().executeQuery("select sequence_owner || '.' || sequence_name from all_sequences  ORDER BY sequence_owner, sequence_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("select sequence_schema || '.' || sequence_name from information_schema.sequences  where sequence_schema not in('information_schema','pg_catalog') order by sequence_schema, sequence_name");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }

        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }


    @Test(priority=9)
    public void validateConstraints() throws SQLException, IOException {
        int index=8;
        String object = "Constraints";
        ora_r = oracon.createStatement().executeQuery("select owner || '.' || Constraint_name from ALL_CONSTRAINTS where constraint_type in ('P','F','C','U') and Constraint_name not like 'SYS%' order by owner, constraint_name");

        List<String> results1 = new ArrayList<String>();
        while(ora_r.next()) {
            results1.add(ora_r.getString(1));
        }

        pg_r = pgcon.createStatement().executeQuery("SELECT constraint_schema || '.' || constraint_name FROM information_schema.table_constraints order by constraint_schema, constraint_name");
        List<String> results2 = new ArrayList<String>();
        while(pg_r.next()) {
            results2.add(pg_r.getString(1));
        }

        int missing = CommonMethods.checkIfPresent(results1,results2,filename1,object);

        if(missing==0){
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("PASS");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(true,true);
        }else{
            FileInputStream file = new FileInputStream(new File(filename1));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row=sheet.createRow(index+1);
            row.createCell(0).setCellValue(object);
            row.createCell(1).setCellValue("FAIL");
            row.createCell(2).setCellValue(results1.size());
            row.createCell(3).setCellValue(missing);
            FileOutputStream fos = new FileOutputStream(new File(filename1));
            workbook.write(fos);
            fos.close();
            file.close();

            Assert.assertEquals(false,true);
        }

    }


}
