package Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.util.PSQLException;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonMethods {


    public static String getCurrentDateTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm");
        String formattedDate = myDateObj.format(myFormatObj);

        return formattedDate;
    }

    //This method compares ResultSets record - by - record
    public static Integer[] compareResultSets(ResultSet result1, ResultSet result2, String filename, String tablename) throws SQLException, IOException, IOException {

        int noOfMismatches = 0;
        int  rowno2 = 1,exrow=0,flag=0;
        Integer[] arr= new Integer[2];

        FileInputStream file = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.createSheet(tablename);
        int perfectmatch=0;

        try {
            while (result1.next()) {

                result2.next();
                int mismatchmatch=0;
                int colcount = result1.getMetaData().getColumnCount();

                for (int i = 1; i <= colcount; i++) {

                    if (result2.getMetaData().getColumnTypeName(i).equalsIgnoreCase("bool")) {

                        if (result1.getBoolean(i) != result2.getBoolean(i)) {
                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;
                            noOfMismatches++;

                        }

                    }
                    else if (result2.getMetaData().getColumnTypeName(i).equalsIgnoreCase("bpchar")) {

                        String s1= StringUtils.deleteWhitespace(result1.getString(i));
                        String s2=StringUtils.deleteWhitespace(result2.getString(i));

                        if (!StringUtils.equalsIgnoreCase(s1,s2)) {
                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;
                            noOfMismatches++;

                        }

                    }

                    else if (result2.getMetaData().getColumnTypeName(i).equalsIgnoreCase("numeric")) {

                        if (Float.compare(result1.getFloat(i),result2.getFloat(i))!=0) {
                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;

                            noOfMismatches++;

                        }

                    }
                    else if (result2.getMetaData().getColumnTypeName(i).equalsIgnoreCase("xml")) {
                        String x1=null;
                        String x2=null;
                        if((result1.getObject(i) ==null) && (result2.getObject(i) == null)){
                            System.err.println("xmltype is null");
                            continue;
                        }else if((result1.getObject(i) !=null) && (result2.getObject(i) == null)){
                            SQLXML xml1 = result1.getSQLXML(i);
                            x1= xml1.getString();
                            x2="null";
                        }else if((result1.getObject(i) ==null) && (result2.getObject(i) != null)){
                            SQLXML xml2 = result2.getSQLXML(i);
                            x2= xml2.getString();
                            x1="null";
                        }else if((result1.getObject(i) !=null) && (result2.getObject(i) != null)){
                            SQLXML xml2 = result2.getSQLXML(i);
                            x2= xml2.getString();
                            SQLXML xml1 = result1.getSQLXML(i);
                            x1= xml1.getString();
                        }

                        if (!StringUtils.equalsIgnoreCase(x1,x2)) {
                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(x1);
                            row2.createCell(4).setCellValue(x2);
                            exrow++;
                            mismatchmatch++;

                            noOfMismatches++;

                        }

                    }else {
                        if (!StringUtils.equalsIgnoreCase(result1.getString(i), result2.getString(i))) {

                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;

                            noOfMismatches++;

                        }
                    }

                }
                if(mismatchmatch==0){
                    perfectmatch++;
                }
                rowno2++;
            }

        } catch (PSQLException e){
            if(noOfMismatches!=0){
                Row row3 = sheet.createRow(exrow+1);
                row3.createCell(0).setCellValue("Total records not equal");
                FileOutputStream fos = new FileOutputStream(new File(filename));
                workbook.write(fos);
                fos.close();
                file.close();
            }
            arr[0]=perfectmatch;
            arr[1]=0;
            System.out.println("Total records not equal");
            return arr;
        }

        if (noOfMismatches > 0) {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            workbook.write(fos);
            fos.close();
            file.close();
            arr[0]=perfectmatch;
            arr[1]=0;
            return arr;
        } else {
            arr[0]=perfectmatch;
            arr[1]=1;
            return arr;
        }
    }


    public static Integer[] compareResultSets_basic(ResultSet result1, ResultSet result2,String filename, String tablename) throws SQLException, IOException {

        int noOfMismatches = 0;
        int  rowno2 = 1,exrow=0,flag=0;
        Integer[] arr= new Integer[2];

        FileInputStream file = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.createSheet(tablename);
        int perfectmatch=0;

        try {
            while (result1.next()) {

                result2.next();
                int mismatchmatch=0;
                int colcount = result1.getMetaData().getColumnCount();

                for (int i = 1; i <= colcount; i++) {

                    if (result2.getMetaData().getColumnTypeName(i).equalsIgnoreCase("bpchar")) {

                        String s1=StringUtils.deleteWhitespace(result1.getString(i));
                        String s2=StringUtils.deleteWhitespace(result2.getString(i));

                        if (!StringUtils.equalsIgnoreCase(s1,s2)) {
                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;
                            noOfMismatches++;

                        }

                    }

                    else {
                        if (!StringUtils.equalsIgnoreCase(result1.getString(i), result2.getString(i))) {

                            if (flag == 0) {
                                Row row = sheet.createRow(exrow);
                                row.createCell(0).setCellValue("Row number");
                                row.createCell(1).setCellValue("Column number");
                                row.createCell(2).setCellValue("Column Name");
                                row.createCell(3).setCellValue("Source");
                                row.createCell(4).setCellValue("Target");
                                exrow++;

                                flag = 1;
                            }

                            Row row2 = sheet.createRow(exrow);
                            row2.createCell(0).setCellValue(rowno2);
                            row2.createCell(1).setCellValue(i);
                            row2.createCell(2).setCellValue(result1.getMetaData().getColumnLabel(i));
                            row2.createCell(3).setCellValue(result1.getString(i));
                            row2.createCell(4).setCellValue(result2.getString(i));
                            exrow++;
                            mismatchmatch++;

                            noOfMismatches++;

                        }
                    }

                }
                if(mismatchmatch==0){
                    perfectmatch++;
                }
                rowno2++;
            }

        } catch (PSQLException e){
            if(noOfMismatches!=0){
                Row row3 = sheet.createRow(exrow+1);
                row3.createCell(0).setCellValue("Total records not equal");
                FileOutputStream fos = new FileOutputStream(new File(filename));
                workbook.write(fos);
                fos.close();
                file.close();
            }
            arr[0]=perfectmatch;
            arr[1]=0;
            System.out.println("Total records not equal");
            return arr;
        }

        if (noOfMismatches > 0) {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            workbook.write(fos);
            fos.close();
            file.close();
            arr[0]=perfectmatch;
            arr[1]=0;
            return arr;
        } else {
            arr[0]=perfectmatch;
            arr[1]=1;
            return arr;
        }
    }

    //This method compares column attributes using jdbc built-in getMetadata() interface
    public static int compareColumnMeta(ResultSet result1, ResultSet result2, String filename, String schema, String tablename, int exrow) throws SQLException, IOException {
        Map<String, Integer> DataTypeMap_pg = new HashMap<String, Integer>();
        DataTypeMap_pg.put("bigserial", 1);
        DataTypeMap_pg.put("int8", 1);
        DataTypeMap_pg.put("bool", 1);
        DataTypeMap_pg.put("bpchar", 2);
        DataTypeMap_pg.put("varchar", 3);
        DataTypeMap_pg.put("date", 4);
        DataTypeMap_pg.put("float8", 1);
        DataTypeMap_pg.put("int4", 1);
        DataTypeMap_pg.put("text", 5);
        DataTypeMap_pg.put("timestamp", 6);
        DataTypeMap_pg.put("xml", 7);
        DataTypeMap_pg.put("bytea", 8);
        DataTypeMap_pg.put("numeric", 1);


        Map<String, Integer> DataTypeMap_o = new HashMap<String, Integer>();
        DataTypeMap_o.put("NUMBER", 1);
        DataTypeMap_o.put("CHAR", 2);
        DataTypeMap_o.put("VARCHAR2", 3);
        DataTypeMap_o.put("DATE", 4);
        DataTypeMap_o.put("CLOB", 5);
        DataTypeMap_o.put("TIMESTAMP", 6);
        DataTypeMap_o.put("SYS.XMLTYPE", 7);
        DataTypeMap_o.put("NVARCHAR2", 3);
        DataTypeMap_o.put("RAW", 8);

        int c1=0, c2=0, noOfMismatches=0;


        FileInputStream file = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(1);

        c1=result1.getMetaData().getColumnCount();
        c2=result2.getMetaData().getColumnCount();
        if(c1==c2){
            for(int i=1;i<=c1;i++){
                if(!StringUtils.equalsIgnoreCase(result1.getMetaData().getColumnLabel(i),result2.getMetaData().getColumnLabel(i))){
                    Row row2 = sheet.createRow(exrow);
                    row2.createCell(0).setCellValue(tablename);
                    row2.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(2).setCellValue("Column name");
                    row2.createCell(3).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(4).setCellValue(result2.getMetaData().getColumnLabel(i));
                    exrow++;


                }
                String Type1=result1.getMetaData().getColumnTypeName(i);
                String Type2=result2.getMetaData().getColumnTypeName(i);
                if(DataTypeMap_o.get(Type1)!=DataTypeMap_pg.get(Type2)){
                    Row row2 = sheet.createRow(exrow);
                    row2.createCell(0).setCellValue(tablename);
                    row2.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(2).setCellValue("Column type");
                    row2.createCell(3).setCellValue(result1.getMetaData().getColumnTypeName(i));
                    row2.createCell(4).setCellValue(result2.getMetaData().getColumnTypeName(i));
                    exrow++;


                }
                if(result1.getMetaData().isNullable(i)!=result2.getMetaData().isNullable(i)){
                    Row row2 = sheet.createRow(exrow);
                    row2.createCell(0).setCellValue(tablename);
                    row2.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(2).setCellValue("Column type");
                    row2.createCell(3).setCellValue(result1.getMetaData().getColumnTypeName(i));
                    row2.createCell(4).setCellValue(result2.getMetaData().getColumnTypeName(i));
                    exrow++;


                }
                if(result1.getMetaData().getPrecision(i)!=result2.getMetaData().getPrecision(i)){
                    Row row2 = sheet.createRow(exrow);
                    row2.createCell(0).setCellValue(tablename);
                    row2.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(2).setCellValue("precision");
                    row2.createCell(3).setCellValue(result1.getMetaData().getPrecision(i));
                    row2.createCell(4).setCellValue(result2.getMetaData().getPrecision(i));
                    row2.createCell(5).setCellValue(result1.getMetaData().getColumnTypeName(i));
                    row2.createCell(6).setCellValue(result2.getMetaData().getColumnTypeName(i));


                    exrow++;


                }
                if(result1.getMetaData().getScale(i)!=result2.getMetaData().getScale(i)){
                    Row row2 = sheet.createRow(exrow);
                    row2.createCell(0).setCellValue(tablename);
                    row2.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(i));
                    row2.createCell(2).setCellValue("scale");
                    row2.createCell(3).setCellValue(result1.getMetaData().getScale(i));
                    row2.createCell(4).setCellValue(result2.getMetaData().getScale(i));
                    row2.createCell(5).setCellValue(result1.getMetaData().getColumnTypeName(i));
                    row2.createCell(6).setCellValue(result2.getMetaData().getColumnTypeName(i));

                    exrow++;


                }


            }

        }else{
            Row row2 = sheet.createRow(exrow);
            row2.createCell(0).setCellValue(tablename);
            row2.createCell(1).setCellValue("NA");
            row2.createCell(2).setCellValue("Number of columns");
            row2.createCell(3).setCellValue(c1);
            row2.createCell(4).setCellValue(c2);
            exrow++;
            for(int y=1; y<=c1;y++){
                int match=0;
                for(int z=1; z<=c2; z++){
                    if(result1.getMetaData().getColumnLabel(y).toLowerCase().equals(result2.getMetaData().getColumnLabel(z))){
                        match++;
                        break;
                    }
                }
                if(match==0){
                    Row row3 = sheet.createRow(exrow);
                    row3.createCell(0).setCellValue(tablename);
                    row3.createCell(1).setCellValue(result1.getMetaData().getColumnLabel(y));
                    row3.createCell(2).setCellValue("Missing column");
                    exrow++;
                }
            }
            FileOutputStream fos = new FileOutputStream(new File(filename));
            workbook.write(fos);
            fos.close();
            file.close();


            return exrow;
        }
        if(noOfMismatches==0){

            FileOutputStream fos = new FileOutputStream(new File(filename));
            workbook.write(fos);
            fos.close();
            file.close();
            return exrow;
        }else
            return 0;
    }

    //This method compares 2 lists and prints values from list 1 that are not present in list 2
    //returns number of missing values
    public static int checkIfPresent(List r1, List r2, String filename, String Object) throws IOException {
        int mismatch=0, flag=0, exrow=0;
        FileInputStream file = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.createSheet(String.format("Missing %s",Object));

        for(int i=0;i<r1.size();i++){
            int match=0;
            for(int j=0;j<r2.size();j++){
                System.out.println(r2.get(j));
                if(StringUtils.equalsIgnoreCase(r1.get(i).toString(),r2.get(j).toString())){
                    match++;
                    break;
                }
            }
            if(match==0){
                if (flag == 0) {
                    Row row = sheet.createRow(exrow);
                    row.createCell(0).setCellValue("Object_Name");

                    exrow++;

                    flag = 1;
                }

                Row row2 = sheet.createRow(exrow);
                row2.createCell(0).setCellValue(r1.get(i).toString());


                exrow++;
                System.out.println("Missing row: "+ i + " " + r1.get(i));
                mismatch++;
            }

        }
        if(mismatch>0){
            FileOutputStream fos = new FileOutputStream(new File(filename));
            workbook.write(fos);
            fos.close();
            file.close();

        }

        return mismatch;

    }


}
