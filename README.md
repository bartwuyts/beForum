beForum
=======
- make sure you have java 8 installed and enabled
- make sure you have mongod running
- import "postcodes.csv" into mongo, using this command:
  mongoimport -d beforum -c zipcodes --type csv --file postcodes.csv --headerline
- run as "Spring Boot App"
- Go to Windows Start->All Programs->Java->Configure Java->Security.
  Add https://localhost:8080 to "Exception site list"
- in your browser, go to https://localhost:8080