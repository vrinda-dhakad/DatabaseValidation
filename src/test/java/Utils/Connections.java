package Utils;

public class Connections {

    //Oracle connection details
    public static String TNS_path="";  //Set path of tns.ora file
    public static String OraCatalogName="";  //Set database name
    public static String OraUsername="";  //Set database username
    public static String OraPassword="";  //Set database password
    public static String OraHostName="";  //Set server hostname
    public static String OradbURL=String.format("jdbc:oracle:thin:@%s",OraCatalogName);

    //PostgreSQL connection details
    public static String PGCatalogName="";  //Set database name
    public static String PGHostName="";  //Set server hostname
    public static String PGUsername="";  //Set database username
    public static String PGPassword="";  //Set database password
    public static String PGdbURL=String.format("jdbc:postgresql://%s:5432/%s",PGHostName,PGCatalogName);

    //Set to path where you want to save result files
    public static String filepath = "";

}
