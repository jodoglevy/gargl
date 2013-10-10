# Gargl Template Files

### Overview

This folder contains Gargl template files which have already been recorded for various web sites, as well as a sample Gargl template file to show the schema. If you generate a new Gargl template for a website and would like to open source it, this is the proper folder where it should be stored.

### Schema

Gargl template files contain a json object composed of the following fields:

- garglSchemaVersion
- moduleVersion
- moduleName
- moduleDescription

function names cannot contain spaces
module names cannot contain spaces
request headers should not contain "Cookie," "Content-Type," or "Content-Length" headers 
