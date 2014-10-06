beForum
=======
- UI design (file UI.ep) is created with Evolus Pencil
- make sure you have java 8 installed and enabled
- make sure you have mongod running
- get list of zipcodes into mongo using this command:
  mongorestore --db beforum --collection zip .\mongozip\beforum\zip.bson
- run as "Spring Boot App"
- Go to Windows Start->All Programs->Java->Configure Java->Security.
  Add https://localhost:8080 to "Exception site list"
- in your browser, go to https://localhost:8080