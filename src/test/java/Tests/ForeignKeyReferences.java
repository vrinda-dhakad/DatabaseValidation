package Tests;

import Utils.CommonMethods;
import Utils.Connections;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

public class ForeignKeyReferences {
    String filename1= String.format("%sFKReferences%s.xlsx", Connections.filepath, CommonMethods.getCurrentDateTime());

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
        row.createCell(0).setCellValue("FKNAME");
        row.createCell(1).setCellValue("ORA_REF");
        row.createCell(2).setCellValue("PG_REF");
        row.createCell(3).setCellValue("RESULT");
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

    @Test
    public void testFKreferences() throws SQLException, IOException {
        ora_s=oracon.createStatement();
        ora_r=ora_s.executeQuery("SELECT a.constraint_name,c.r_owner|| '.' || c_pk.table_name || '.' || a.column_name\n" +
                "                 FROM all_cons_columns a\n" +
                "                  JOIN all_constraints c ON a.owner = c.owner\n" +
                "                                       AND a.constraint_name = c.constraint_name\n" +
                "                  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n" +
                "                                          AND c.r_constraint_name = c_pk.constraint_name\n" +
                "                WHERE c.constraint_type = 'R' \n" +
                "              order by c.r_owner,c_pk.table_name , a.column_name");

        pg_s=pgcon.createStatement();
        pg_r=pg_s.executeQuery(String.format("select  kcu.constraint_name, kcu.table_schema || '.' ||rel_tco.table_name || '.' || string_agg(kcu.column_name, ', ') \n" +
                "from information_schema.table_constraints tco\n" +
                "join information_schema.key_column_usage kcu\n" +
                "          on tco.constraint_schema = kcu.constraint_schema\n" +
                "          and tco.constraint_name = kcu.constraint_name\n" +
                "join information_schema.referential_constraints rco\n" +
                "          on tco.constraint_schema = rco.constraint_schema\n" +
                "          and tco.constraint_name = rco.constraint_name\n" +
                "join information_schema.table_constraints rel_tco\n" +
                "          on rco.unique_constraint_schema = rel_tco.constraint_schema\n" +
                "          and rco.unique_constraint_name = rel_tco.constraint_name\n" +
                "where tco.constraint_type = 'FOREIGN KEY' " +
                "group by kcu.table_schema,\n" +
                "         kcu.table_name,\n" +
                "         rel_tco.table_name,\n" +
                "         rel_tco.table_schema,\n" +
                "         kcu.constraint_name\n" +
                "order by kcu.table_schema,\n" +
                "\t\t\tkcu.table_schema,\n" +
                "         kcu.table_name;"));

        List<String[]> OracleList=new ArrayList<>();
        while(ora_r.next()){

            OracleList.add(new String[]{ora_r.getString(1),ora_r.getString(2)});

        }

        List<String[]> PGList=new ArrayList<>();
        while(pg_r.next()){

            PGList.add(new String[]{pg_r.getString(1),pg_r.getString(2)});

        }
        int count=0;

        for(int i=0;i<OracleList.size();i++){
            int match=0;
            String[] s1=OracleList.get(i);
            for(int j=0;j<PGList.size();j++){

                String[] s2=PGList.get(j);
                System.out.println(s1[1] + " " + s2[1]);
                if(StringUtils.equalsIgnoreCase(s1[0],s2[0])){
                    match++;
                    if(StringUtils.equalsIgnoreCase(s1[1],s2[1])){
                        FileInputStream file = new FileInputStream(new File(filename1));
                        Workbook workbook = new XSSFWorkbook(file);
                        Sheet sheet = workbook.getSheetAt(0);
                        Row row = sheet.createRow(count + 1);
                        row.createCell(0).setCellValue(s1[0]);
                        row.createCell(1).setCellValue(s1[1]);
                        row.createCell(2).setCellValue(s2[1]);
                        row.createCell(3).setCellValue("PASS");
                        FileOutputStream fos = new FileOutputStream(new File(filename1));
                        workbook.write(fos);
                        fos.close();
                        file.close();
                        count++;
                        PGList.remove(j);
                        break;
                    }
                    else if(!StringUtils.equalsIgnoreCase(s1[1],s2[1])){
                        count++;
                        FileInputStream file = new FileInputStream(new File(filename1));
                        Workbook workbook = new XSSFWorkbook(file);
                        Sheet sheet = workbook.getSheetAt(0);
                        Row row = sheet.createRow(count + 1);
                        row.createCell(0).setCellValue(s1[0]);
                        row.createCell(1).setCellValue(s1[1]);
                        row.createCell(2).setCellValue(s2[1]);
                        row.createCell(3).setCellValue("FAIL");
                        FileOutputStream fos = new FileOutputStream(new File(filename1));
                        workbook.write(fos);
                        fos.close();
                        file.close();
                    }
                }

            }
            if(match==0){
                FileInputStream file = new FileInputStream(new File(filename1));
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet = workbook.getSheetAt(0);
                Row row = sheet.createRow(count + 1);
                row.createCell(0).setCellValue(s1[0]);
                row.createCell(1).setCellValue(s1[1]);
                row.createCell(2).setCellValue("FK missing in PG");
                row.createCell(3).setCellValue("FAIL");
                FileOutputStream fos = new FileOutputStream(new File(filename1));
                workbook.write(fos);
                fos.close();
                file.close();
                count++;
            }
        }
    }
}
