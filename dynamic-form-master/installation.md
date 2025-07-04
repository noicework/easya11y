#Installation

Maven is the easiest way to install the module. Add the following dependency to your bundle:

```
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-core</artifactId>
      <version>${formVersion}</version>
  </dependency>
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-rest</artifactId>
      <version>${formVersion}</version>
  </dependency>
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-app</artifactId>
      <version>${formVersion}</version>
  </dependency>
```
Add the chosen database JDBC drivers.<br/><br/>
MySQL:
```
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>${mysql.version}</version>
  </dependency>
```
Postgres:
```
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>${postgresql.version}</version>
   </dependency>
```
_If you want to use an IDE like Eclipse or IntelliJ, you will need to configure the userangent due to ebean framework as described in the following link:_
https://ebean.io/docs/getting-started/eclipse-ide

