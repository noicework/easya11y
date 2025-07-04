# Form parent module

This is the parent module of the form related modules

<br>

## Form Core

The Form Core module allows saving forms in external SQL Database.<br>
<br>
To install this module add maven dependency to this module:<br>
```
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-core</artifactId>
      <version>${project.version}</version>
  </dependency>
```
<br><br>
To function properly this module requires configurations in <b>module configuration</b> locations.<br>
Configurations can be located in JCR config node or config.yaml file. <br>
We need to pass database connection info to the module.<br>
This is minimum configuration that needs to be passed to the module so it can connect to a database instance:<br>
```properties
datasource:
  username: [db_user]
  password: [db_assword]
  url: [db_url]
  driver: [db_driver]
  migration:
    path: form-core/dbmigration/[db_type]
    run: [true/false]
```

<br>
<b>example configuration for MySql database server:</b>
<br><br>

```properties
datasource:
  username: user
  password: password
  url: jdbc:mysql://127.0.0.1:3306/forms
  driver: com.mysql.cj.jdbc.Driver
  migration:
    path: form-core/dbmigration/mysql
    run: true
```
<br><br>
All properties that are prefixed with "form." will be passed to ebean server.<br>
This way we can easily tune ebean ORM framework.<br>
More info on Ebean ORM is available on this <a href="https://ebean.io/docs/"> ebean documentation link</a>.
<br><br>
This module does not create a database.<br>
Database MUST EXISTS and database user has to have also CREATE/DROP/ALTER RIGHTS on that database. 
<br><br>
For database connection to work we need appropriate database driver to be available on the classpath.
<br>
For example: if we are using mysql database with tomcat server we need to place "mysql-connector-java-8.0.18.jar" 
in tomcat lib folder.
<br><br>
## Form App

The Form App module allows browsing and managing forms from Magnolia's Custom Content Application.<br>
This module is dependent on Form Core module.<br>
<br>
To install this module add maven dependency to this module:<br>
```
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-app</artifactId>
      <version>${project.version}</version>
  </dependency>
```
<br><br>

## Form Rest

The Form Rest module provide rest api which can be used to manage forms (CRUD operations).<br> 
This module is dependent on Form Core module.<br>
<br>
To install this module add maven dependency to this module:<br>
```
  <dependency>
      <groupId>info.magnolia.form</groupId>
      <artifactId>magnolia-form-rest</artifactId>
      <version>${project.version}</version>
  </dependency>
```
<br><br>

## License

DX Core
