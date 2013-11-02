DROP TABLE Error_Events;
DROP TABLE Errors;
DROP TRIGGER Update_Error_Sources_Timestamp on Error_Sources;
DROP TABLE Error_Sources;
DROP TABLE Person_Permissions;
DROP TABLE Role_Permissions;
DROP TRIGGER Update_Role_Assignments_Timestamp on Role_Assignments;
DROP TABLE Role_Assignments;
DROP TRIGGER Update_Roles_Timestamp on Roles;
DROP TABLE Roles;
DROP TRIGGER Update_Users_Timestamp on Users;
DROP TABLE Users;
DROP FUNCTION Update_Last_Updated_Column();

