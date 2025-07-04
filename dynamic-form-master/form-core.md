#Usage

##Define the database connection

You can configure the database connection in the JCR or the File System (YAML) under:<br/>
_/src/main/resources/form-core/config.yaml_
<br/><br/>

MySQL:<br/>
_/form-core/config.yaml_
```
datasource:
    username: user
    password: password
    url: jdbc:mysql://127.0.0.1:3306/forms
    driver: com.mysql.cj.jdbc.Driver
    migration:
        path: form-core/dbmigration/mysql
        run: true
```

If you have an error like:
```
"Error initialising DataSource: The server time zone value 'CEST' is unrecognized or represents more than one time zone. You must configure either the server or JDBC driver"
```
Add the following code to your url property in config.yaml: "?serverTimezone=Europe/Madrid" or your personal time zone, like described bellow:
```
datasource:
    username: user
    password: password
    url: jdbc:mysql://127.0.0.1:3306/forms?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
    driver: com.mysql.cj.jdbc.Driver
    migration:
        path: form-core/dbmigration/mysql
        run: true
```

PostgrSQL:<br/>
_/form-core/config.yaml_
```
datasource:
    username: user
    password: password
    url: jdbc:postgresql://localhost:5432/forms
    driver: org.postgresql.Driver
    migration:
        path: form-core/dbmigration/mysql
        run: true
```
Note:<br/>
This module does not create a database.<br/>
The database **must exist** and a user having the **CREATE/DROP/ALTER** rights on that database must be defined.

##Database schema

The table(s) required are created automatically by the init scripts in the magnolia-form-core module. For these scripts to be executed the first time the module is installed (or whenever the database is empty) the migration node in the datasource is used:
<br/>

- path: The path of the init script.
    - form-core/dbmigration/mysql for MySQL schema
    - form-core/dbmigration/pg for PostgreSQL schema
- run: this property needs to be true to initialize or migrate database. Default value: true
