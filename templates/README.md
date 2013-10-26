# Gargl Template Files

### Overview

This folder contains Gargl template files which have already been recorded for various web sites, as well as a sample Gargl template file to show the schema. If you generate a new Gargl template for a website and would like to open source it, this is the proper folder where it should be stored.

### Parameterized Values

Certain fields in the gargl *function object* can contain parameterized values. A parameterized value is a value that is not a static string, but rather defined by a parameter to the function it is a part of. To insert a parameterized value, insert the name of the parameter to be added to the function, surrounded by '@' signs. For example, "@title@" would cause a parameter to be added to the function with the name "title."

As of Gargl Schema v1.0, the following fields can contain parameterized values:
- The *url* field of a *request object* can contain one or more parameterized values.
- The *value* field of a *request field object* can contain a single parameterized value.

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
- **request**: A *request object* containing the details of the HTTP request for this function. Described below. Required.
- **response**: A *response object* containing the details of the HTTP response for this function. Described below. Optional.

##### Request object

Each function object contains a single request object. A request object is composed of the following fields:

- **method**: The string HTTP method of the request. Required.
- **url**: The string url of the request. Can contain one or more *parameterized values* or be static. Required.
- **httpVersion**: The string http version of the request. Required.
- **headers**: Any array of *request field objects* to send as request headers. Optional. Request headers should not contain "Cookie," "Content-Type," or "Content-Length" headers. 
- **queryString**: Any array of *request field objects* to send in the request query string. Optional.
- **postData**: The *request post data object* for this request. Optional.

###### Request Post Data object

Each request object can contain a request post data object. A request post data object is composed the following fields:

- **mimeType**: The string mime type of the request body. Required.
- **params**: Any array of *request field objects* to send in the request body. Required.

###### Request Field object

Each request object can contain one or more request field objects. A request field object is composed the following fields:

- **name**: The string name of the request field object. Required.
- **value**: The string value of the request field object. Can contain a single *parameterized value* or be static. Required.
- **description**: The string description of the request field object. Optional.

##### Response object

Each function object can contain a single response object. A response object is composed of the following fields:

- **headers**: Any array of *response header objects* as described below. Optional.
- **fields**: Any array of *response field objects* as described below. Optional.

###### Response Header object

Each response object can contain one or more response header objects. A response header object is composed the following fields:

- **name**: The string name of the response header which the HTTP request returns. Required.

###### Response Field object

Each response object can contain one or more response field objects. A response field object is a value parsed from the returned html in the response body. A response field object is composed the following fields:

- **name**: The string name of the response field. Required.
- **cssSelector**: The string css selector for the html tag in the response body html whose inner html is the value for this response field object. Required.


