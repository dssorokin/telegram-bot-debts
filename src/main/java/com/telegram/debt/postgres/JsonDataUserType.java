package com.telegram.debt.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dsorokin on 04.07.2021
 */
public class JsonDataUserType implements UserType {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public int[] sqlTypes() {
		return new int[]{Types.JAVA_OBJECT };
	}

	@Override
	public Class returnedClass() {
		return Map.class;
	}

	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
		PGobject value = (PGobject) resultSet.getObject(strings[0]);
		if (value != null && value.getValue() != null) {
			try {
				return objectMapper.readValue(value.getValue(), new TypeReference<HashMap<String, BigDecimal>>() {
				});
			} catch (JsonProcessingException e) {
			}
		}
		return new HashMap<>();
	}

	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
		try {
			if (o == null) {
				preparedStatement.setNull(i, Types.OTHER);
			} else {
				preparedStatement.setObject(i, objectMapper.writeValueAsString(o), Types.OTHER);
			}
		} catch (JsonProcessingException e) {
			preparedStatement.setNull(i, Types.OTHER);
		}
	}

	@Override
	public Object deepCopy(Object originalValue) throws HibernateException {
		if (originalValue == null) {
			return null;
		}
		if (!(originalValue instanceof Map)) {
			return null;
		}

		Map resultMap = new HashMap<>();
		Map<?, ?> tempMap = (Map<?, ?>) originalValue;
		tempMap.forEach((key, value) -> resultMap.put((String) key, (String) value));

		return resultMap;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object o) throws HibernateException {
		Object copy = deepCopy(o);

		if (copy instanceof Serializable) {
			return (Serializable) copy;
		}

		return "";
	}

	@Override
	public Object assemble(Serializable serializable, Object o) throws HibernateException {
		return deepCopy(serializable);
	}

	@Override
	public Object replace(Object o, Object o1, Object o2) throws HibernateException {
		return deepCopy(o);
	}

	@Override
	public boolean equals(Object o, Object o1) throws HibernateException {
		return ObjectUtils.nullSafeEquals(o, o1);
	}

	@Override
	public int hashCode(Object o) throws HibernateException {
		if (o == null) {
			return 0;
		}

		return o.hashCode();
	}
}
