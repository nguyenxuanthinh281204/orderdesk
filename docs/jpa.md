# JPA & Hibernate Mapping Documentation

## 1. Schema Validation Failure (`hbm2ddl.auto=validate`)
Thực nghiệm cố ý đổi tên cột `@Column(name = "customer_id_wrong")` trong `Order.java` và khởi động EntityManagerFactory:

```text
org.hibernate.tool.schema.spi.SchemaManagementException: Schema-validation: missing column [customer_id_wrong] in table [orders]
	at org.hibernate.tool.schema.internal.AbstractSchemaValidator.validateColumns(AbstractSchemaValidator.java:181)
	at org.hibernate.tool.schema.internal.AbstractSchemaValidator.validateTable(AbstractSchemaValidator.java:128)
	at org.hibernate.tool.schema.internal.GroupedSchemaValidator.validateTables(GroupedSchemaValidator.java:42)
	at org.hibernate.tool.schema.internal.AbstractSchemaValidator.performValidation(AbstractSchemaValidator.java:98)