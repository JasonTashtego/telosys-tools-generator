/**
 *  Copyright (C) 2008-2017  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.tools.generator.context;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

import org.telosys.tools.commons.NamingStyleConverter;
import org.telosys.tools.commons.StrUtil;
import org.telosys.tools.generator.context.doc.VelocityMethod;
import org.telosys.tools.generator.context.doc.VelocityObject;
import org.telosys.tools.generator.context.exceptions.GeneratorSqlException;
import org.telosys.tools.generator.context.names.ContextName;

//-------------------------------------------------------------------------------------
@VelocityObject(
		contextName=ContextName.SQL,
		text = { 
				"Object for schema creation in SQL language (for a relational database)",
				"It manages :",
				" - table name conversion",
				" - column name conversion",
				" - field type conversion (neutral type to SQL column type)",
				"It is designed to facilitate DDL commands generation",
				"( CREATE TABLE, FOREIGN KEY, etc) ",
				""
		},
		since = "3.4.0"
 )
//-------------------------------------------------------------------------------------
public class SqlInContext {
	// TODO : see also 	AttributeInContext.getSqlType

	private static final String CONV_TABLE_NAME  = "conv.tableName";
	private static final String CONV_COLUMN_NAME = "conv.columnName";
	
	private final NamingStyleConverter converter = new NamingStyleConverter();

	private final String targetDbName ;
	private final String targetDbConfigFile ;
	private final Properties targetDbConfig ;
	private final String tableNameStyle;
	private final String columnNameStyle;

	/**
	 * Constructor
	 * @param targetDbName
	 * @param targetDbConfigFile
	 */
	public SqlInContext(String targetDbName, String targetDbConfigFile) {
		super();
		if ( StrUtil.nullOrVoid(targetDbName) ) {
			throw new GeneratorSqlException("Target database name undefined, cannot create $sql");
		}
		if ( StrUtil.nullOrVoid(targetDbConfigFile) ) {
			throw new GeneratorSqlException("Target database config file undefined, cannot create $sql");
		}
		this.targetDbName = targetDbName;
		this.targetDbConfigFile = targetDbConfigFile ;
		this.targetDbConfig = loadSpecificConfiguration(targetDbConfigFile);
		this.tableNameStyle  = getConfigValue(CONV_TABLE_NAME);
		this.columnNameStyle = getConfigValue(CONV_COLUMN_NAME);
	}
		
	/**
	 * Constructor
	 * @param targetDbName
	 */
	public SqlInContext(String targetDbName) {
		super();
		if ( StrUtil.nullOrVoid(targetDbName) ) {
			throw new GeneratorSqlException("Target database name undefined, cannot create $sql");
		}
		this.targetDbName = targetDbName;
		this.targetDbConfigFile = "target-db/" + targetDbName.trim().toLowerCase() + ".properties" ;
		this.targetDbConfig = loadStandardConfiguration(targetDbConfigFile);
		this.tableNameStyle  = getConfigValue(CONV_TABLE_NAME);
		this.columnNameStyle = getConfigValue(CONV_COLUMN_NAME);
	}
	
	/**
	 * Constructor
	 * @param envInContext
	 */
	// TODO : keep it or not ?
	public SqlInContext(EnvInContext envInContext) {
		this(envInContext.getDatabase());
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Returns the target database name",
			""
		},
		example={	
				"$sql.databaseName()"
			},
		since = "3.4.0"
	)
	public String getDatabaseName() {
		return this.targetDbName;
    }

	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Returns the target database configuration file",
			""
		},
		example={	
				"$sql.databaseConfigFile()"
			},
		since = "3.4.0"
	)
	public String getDatabaseConfigFile() {
		return this.targetDbConfigFile;
    }
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Converts the name of the given entity to table naming style",
			"For example converts 'EmployeeJobs' to 'employee_jobs'",
			""
		},
		example={	
				"$sql.tableName($entity)"
			},
		since = "3.4.0"
	)
	public String tableName(EntityInContext entity) {
		return convertToTableName(entity.getName());
    }
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Converts the given string to table naming style ",
			"For example converts 'EmployeeJobs' to 'employee_jobs'",
			""
		},
		example={	
				"$sql.tableName($var)"
			},
		since = "3.4.0" 
	)
	public String convertToTableName(String originalName) {
		return convertName(originalName, tableNameStyle);
    }
		
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Returns the database column name for the given attribute ",
			"For example 'city_code' for an attribute named 'cityCode'",
			"The database name defined in the model is used in priority",
			"if no database name is defined then the attribute name is converted to database name",
			"by applying the target database conventions",
			""
		},
		parameters = { 
			"attribute : attribute from which to get column name " ,
		},
		example={	
			"$sql.columnName($attribute)"
		},
		since = "3.4.0"
	)
	public String columnName(AttributeInContext attribute) {
		// Check if the attribute has a specific database name in the model
		String databaseName = attribute.getDatabaseName() ;
		if ( StrUtil.nullOrVoid(databaseName) ) {
			// no database name in the model => use standard conversion from attribute name
			return convertToColumnName(attribute.getName());
		}
		else {
			// database name in the model => use it
			return databaseName ;
		}
    }
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Converts the given string to column naming style ",
			"For example converts 'firstName' to 'first_name' ",
			""
		},
		parameters = { 
			"originalName : name to be converted " 
		},
		example={	
			"$sql.convertToColumnName($var)"
		},
		since = "3.4.0"
	)
	public String convertToColumnName(String originalName) {
		return convertName(originalName, columnNameStyle);
    }
		
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Converts the attribute neutral type to the corresponding SQL type ",
			"For example converts 'string' to 'varchar(x)' ",
			""
		},
		parameters = { 
			"attribute : attribute from which to get column type " ,
		},
		example={	
				"$sql.columnType($attribute)"
			},
		since = "3.4.0"
	)
	public String columnType(AttributeInContext attribute) {
		// Check if the attribute has a specific database type in the model
		String databaseType = attribute.getDatabaseType() ;
		if ( StrUtil.nullOrVoid(databaseType) ) {
			// not defined in the model : try to convert neutral type
			return convertToColumnType(attribute.getNeutralType(), attribute.isAutoIncremented(),
								getMaximumSize(attribute), getPrecision(attribute));
		}
		else {
			// defined in the model => use it as is
			return databaseType ;
		}
    }
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Converts the given neutral type to column type ",
			"For example converts 'string' to 'varchar(20)' ",
			""
		},
		example={	
			"$sql.convertToColumnType('string', false, 20, 0)"
			},
		parameters = { 
			"neutralType : neutral type to be converted " ,
			"autoInc : auto-incremented attribute (true/false)",
			"size : maximum size (for a variable length string)",
			"precision : precision and scale (x.y) for a numeric attribute"
		},
		since = "3.4.0"
	)
	public String convertToColumnType(String neutralType, boolean autoInc, 
			Integer size, BigDecimal precision) {
		// get SQL type from database config
		String sqlType = getConfigType(neutralType, autoInc);
		
		// replace size or precision if any 
		if ( sqlType.contains("%") ) {
			return replaceVar(sqlType, size, precision);
		}
		else {
			return sqlType;
		}
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod ( 
		text= { 
			"Returns the column constraintes for the given attribute",
			"For example : NOT NULL DEFAULT 12",
			""
		},
		parameters = { 
			"attribute : attribute from which to get column constraints " ,
		},
		example={	
			"$sql.columnConstraints($attribute)"
		},
		since = "3.4.0"
	)
	public String columnConstraints(AttributeInContext attribute) {
		StringBuilder sb = new StringBuilder();
		
		//--- NOT NULL
		if ( attribute.isDatabaseNotNull() || attribute.isNotNull() ) {
			sb.append("NOT NULL");
		}
		
		//--- DEFAULT
		String defaultValue = null ;
		if ( attribute.hasDatabaseDefaultValue() ) {
			defaultValue = attribute.getDatabaseDefaultValue();
		}
		else if ( attribute.hasDefaultValue() ) {
			defaultValue = attribute.getDefaultValue();
		}
		if ( defaultValue != null ) {
			if ( sb.length() > 0 ) {
				sb.append(" ");
			}
			sb.append("DEFAULT ");
			if ( attribute.isStringType() ) {
				sb.append("'").append(defaultValue).append("'");
			}
			else {
				sb.append(defaultValue);
			}
		}
		return sb.toString();
    }
		
	//-------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
	private Properties loadStandardConfiguration(String propFileName) {
		Properties properties = new Properties();
		ClassLoader classLoader = this.getClass().getClassLoader();
		try ( InputStream inputStream = classLoader.getResourceAsStream(propFileName)) {
			if ( inputStream == null ) {
				throw new GeneratorSqlException("Database config file '" 
						+ propFileName + "' not found in the classpath");
			}
			properties.load(inputStream);
		} catch (IOException e) {
			throw new GeneratorSqlException("Cannot load database config file '" 
					+ propFileName + "' IOException");
		}
		return properties;
	}
	//-------------------------------------------------------------------------------------
	private Properties loadSpecificConfiguration(String propFileName) {
		Properties properties = new Properties();
		// TODO
		return properties;
	}
	//-------------------------------------------------------------------------------------
	private String getConfigValue(String key) {
		String val = this.targetDbConfig.getProperty(key);
		if ( val != null ) {
			return val.trim();
		}
		else {
			throw new GeneratorSqlException("getConfigValue", 
					"Cannot get config value for key '"+ key + "'");
		}
	}
	//-------------------------------------------------------------------------------------
	/**
	 * Name conversion 
	 * @param originalName
	 * @param styleName style to be used ( snake_case, ANACONDA_CASE, camelCase, PascalCase)
	 * @return
	 */
	protected String convertName(String originalName, String styleName) {
		switch (styleName) {
		case "snake_case" :
			return converter.toSnakeCase(originalName);
		case "ANACONDA_CASE" :
			return converter.toAnacondaCase(originalName);
		case "camelCase" :
			return converter.toCamelCase(originalName);
		case "PascalCase" :
			return converter.toPascalCase(originalName);
		default :
			throw new GeneratorSqlException("convertName", 
					"Unknown style '" + styleName + "'");
		}
    }
	private BigDecimal toBigDecimal(String s) {
		if ( s != null ) {
			String v = s.trim();
			try {
				return new BigDecimal(v);
			} catch (NumberFormatException e) {
				throw new GeneratorSqlException("invalid attribute size/length '" + v + "' NumberFormatException");
			}
		}
		else {
			throw new GeneratorSqlException("attribute size/length is null");
		}
	}
	private Integer getMaximumSize(AttributeInContext attribute) {
		BigDecimal size = null ;
		// use @DbSize first : eg @DbSize(45)
		if ( ! StrUtil.nullOrVoid(attribute.getDatabaseSize()) ) {
			size = toBigDecimal( attribute.getDatabaseSize() );
		} 
		// use @SizeMax if any : eg @SizeMax(45)
		else if ( ! StrUtil.nullOrVoid(attribute.getMaxLength()) ) {
			size = toBigDecimal( attribute.getMaxLength() );
		}
		// @DbSize can contains something like "8.2" => keep int part only 
		if ( size != null ) {
			return Integer.valueOf(size.intValue());
		}
		return null ;
	}
	private BigDecimal getPrecision(AttributeInContext attribute) {
		// use @DbSize first : eg @DbSize(10.2) or @DbSize(8)
		if ( ! StrUtil.nullOrVoid(attribute.getDatabaseSize()) ) {
			return toBigDecimal( attribute.getDatabaseSize() );
		} 
		// TODO : add @Precision(xx) annotation ????
//		else if ( ! StrUtil.nullOrVoid(attribute.getPrecision()) ) {
//			return toBigDecimal( attribute.getPrecision() );
//		}
		return null ;
	}
		
	
	//-------------------------------------------------------------------------------------
	
	protected String getConfigType(String originalType, boolean autoIncremented) {
		// Map entry examples :
		// type.int           = integer
		// type.int.autoincr  = serial
		String key = "type." + originalType.trim();
		if ( autoIncremented ) {
			String keyAutoIncr = key + ".autoincr" ;
			String type = this.targetDbConfig.getProperty(keyAutoIncr);
			if ( type != null ) {
				// specific auto-incremented type found
				return type ; 
			}
			else {
				// specific auto-incremented type NOT found
				// get standard type
				return getConfigValue(key); 
			}
		}
		else {
			return getConfigValue(key);
		}
	}
	
	//-------------------------------------------------------------------------------------

	private void checkSizeValue(String sqlType, Integer size) {
		if ( size != null && size.intValue() <= 0 ) {
			throw new GeneratorSqlException("SQL type '" + sqlType + "' : invalid size " + size);
		}
	}
	private void checkPrecisionValue(String sqlType, BigDecimal precision) {
		if ( precision != null && precision.intValue() <= 0 ) {
			throw new GeneratorSqlException("SQL type '" + sqlType + "' : invalid precision " + precision);
		}
	}
	
	/**
	 * Replaces size parameter (%s) or (%S) for types like 'varchar(..)'
	 * @param sqlType
	 * @param size 
	 * @return
	 */
	protected String replaceVarSize(String sqlType, Integer size) {
		if ( sqlType.contains("%S") ) {
			// SIZE IS MANDATORY 
			checkSizeValue(sqlType, size);
			if ( size != null ) {
				return StrUtil.replaceVar(sqlType, "%S", size.toString());
			}
			else {
				throw new GeneratorSqlException("SQL type '" + sqlType + "' : size is mandatory");
			}
		}
		else if ( sqlType.contains("%s") ) {
			checkSizeValue(sqlType, size);
			// SIZE IS OPTIONAL  
			if ( size != null ) {
				return StrUtil.replaceVar(sqlType, "%s", size.toString());
			}
			else {
				return StrUtil.replaceVar(sqlType, "(%s)", ""); // remove
			}
		}
		else {
			throw new GeneratorSqlException("SQL type '" + sqlType + "' : internal error (size var)");
		}
	}
	
	/**
	 * Replaces precision (and scale) parameter (%p) or (%P) <br>
	 * for types like NUMBER(8), NUMBER(8.2), numeric(6.2)
	 * @param sqlType
	 * @param precision
	 * @return
	 */
	protected String replaceVarPrecision(String sqlType, BigDecimal precision) {
		if ( sqlType.contains("%P") ) {
			// PRECISION IS MANDATORY 
			checkPrecisionValue(sqlType, precision);
			if ( precision != null ) {
				return StrUtil.replaceVar(sqlType, "%P", precision.toString());
			}
			else {
				throw new GeneratorSqlException("SQL type '" + sqlType + "' error : invalid precision " + precision);
			}
		}
		else if ( sqlType.contains("%p") ) {
			// PRECISION IS OPTIONAL 
			checkPrecisionValue(sqlType, precision);
			if ( precision != null ) {
				return StrUtil.replaceVar(sqlType, "%p", precision.toString());
			}
			else {
				return StrUtil.replaceVar(sqlType, "(%p)", ""); // remove
			}
		}
		else {
			throw new GeneratorSqlException("SQL type '" + sqlType + "' : internal error (precision var)");
		}
	}
	protected String replaceVar(String sqlType, Integer size, BigDecimal precision) {
		if ( sqlType.contains("%S") || sqlType.contains("%s") ) {
			// Size,  eg VARCHAR(8)
			return replaceVarSize(sqlType, size);
		}
		else if ( sqlType.contains("%P") || sqlType.contains("%p")) {
			// Precision [and scale], eg NUMBER(8), NUMBER(8.2), numeric(6.2)
			return replaceVarPrecision(sqlType, precision);
		}
		else {
			return sqlType;
		}
	}
	
	//-------------------------------------------------------------------------------------
}
