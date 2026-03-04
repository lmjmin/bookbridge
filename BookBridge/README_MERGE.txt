Merged for graduation:
- Base: BookBridge (1)
- Compatible signup: (1 + boram) via updated SignupRequest & AuthController
- BookSearchController unified at /api/books/search (Kakao API)
- application.properties kept from (1); MySQL config in application-local.properties
- signup.html enhanced with birthdate/phone formatting

Set Kakao key in src/main/resources/application.properties:
kakao.api.key=cf9387f7a0ab9be56227bb46432a87fe

Run dev (H2): default
Run with MySQL: --spring.profiles.active=local