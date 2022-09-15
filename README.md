# DatabaseValidation
**Description:** Oracle to PostgreSQL Database Validation is a Java based testing suite for validating database migration from Oracle to PostgreSQL. Validation results are written in excel files

**Built with:**
JDBC,
TestNG,
Apache POI,
Apache Commons

**Features:**
1. Database Objects Validation (Schemas, Tables, Views, Indexes, Procedures, Functions, Triggers, Sequences, Constraints)
2. Tables data validation
3. Foreign key references validation
4. Columns attributes (Metadata) Validation
	
**Dependencies:**
1. Java 1.8 or higher
2. Maven
3. IntellijIdea
	
**Set Up:**
1. Download and unzip DatabaseValidation.zip
2. Open IntelliJIdea> Import> New Project> Navigate to unzipped folder> select pom.xml> OK
3. Import maven dependencies
4. *(Optional)* Set Up SDK to Java 1.8 or higher
5. Configure following paths according to your system
6. Open src\main\java\Utils\Connections.java
7. Set database connection details
8. Set filepath to local path where you wish to save result files

**How to use:**
1. For objects validation simply navigate to src\main\java\Tests\ObjectsValidation.java > Run Test
2. For Tables data validation, navigate to src\main\java\Tests\TablesDataBySchema.java > Set schemaname > Run Test
3. For Columns Metadata validation, navigate to src\main\java\Tests\ColumnsMetadataBySchema.java > Set schemaname > Run Test
4. For Foreign Key References validation navigate to src\main\java\Tests\ForeignKeyReferences.java > Run Test

**Contact:**
John Astarita- john.astarita@us.ibm.com
Vrinda Dhakad- vrinda.dhakad@ibm.com
