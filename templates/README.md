# Gargl Template Files

### Overview

This folder contains Gargl template files which have already been recorded for various web sites, as well as a sample Gargl template file to show the schema. If you generate a new Gargl template for a website and would like to open source it, this is the proper folder where it should be stored.

### Schema v1.0

Gargl template files contain a json object composed of the following fields:

- **garglSchemaVersion**: The string version of the gargl schema. Current latest schema is "1.0". Required.
- **moduleVersion**: The string version of the website this gargl template file was generated against. Required.
- **moduleName**: The string name to be used for the module when it is generated. Cannot contain spaces. Required.
- **moduleDescription**: A string description of the module. Optional.
- **functions**: An array of *function objects* as described below. Required.

#### Function Object

A gargl template file contains one or more function objects. A function object is composed of the following fields:

- **functionName**: The string name to be used for this function when a module is generated. Cannot contain spaces. Required.
- **functionDescription**: A string description of the function. Optional.
- **request**: A *request object* containing the details of the HTTP request for this function. Described below. Required
- **response**: A *response object* containing the details of the HTTP response for this function. Described below. Optional

##### Request object

Each function object contains a single request object. A request object is composed of the following fields:

TBD

request headers should not contain "Cookie," "Content-Type," or "Content-Length" headers 

##### Response object

Each function object can contain a single response object. A response object is composed the following fields:

- **headers**: Any array of *response header objects* as described below. Optional

###### Response Header object

Each response object can contain one or more response header objects. A response header object is composed the following fields:

- **name**: The string name of the response header which the HTTP request returns. Required.

