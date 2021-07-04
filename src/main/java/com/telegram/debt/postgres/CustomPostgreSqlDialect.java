package com.telegram.debt.postgres;

import org.hibernate.dialect.PostgreSQL94Dialect;

import java.sql.Types;

/**
 * @author dsorokin on 04.07.2021
 */
public class CustomPostgreSqlDialect extends PostgreSQL94Dialect {

	public CustomPostgreSqlDialect() {
		this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
	}
}
