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

import java.util.List;

import org.telosys.tools.commons.StrUtil;
import org.telosys.tools.generator.context.doc.VelocityMethod;
import org.telosys.tools.generator.context.doc.VelocityObject;
import org.telosys.tools.generator.context.names.ContextName;
import org.telosys.tools.generator.context.tools.LinesBuilder;

//-------------------------------------------------------------------------------------
@VelocityObject(
	contextName=ContextName.CSHARP,
	text = { 
		"Object providing a set of utility functions for C# language code generation",
		""
	},
	since = "4.1.0"
 )
//-------------------------------------------------------------------------------------
public class CsharpInContext {

	@VelocityMethod(
		text={	
			"Returns the C# type with a question mark ('?') at the end if the attribute is nullable",
			"for example: 'string?' if nullable, else 'string' "
			},
		parameters = { 
				"attribute : the attribute (nullable or not nullable)"
			},
		example = {
				"$csharp.nullableType($attribute) " 
			}
		)
    public String nullableType( AttributeInContext attribute ) {
		if ( attribute != null ) {
			String type = attribute.getType();
			// add "?" if "nullable"			
			if ( attribute.isNotNull() ) {
				// NOT NULL => return type as is : "string", "int", etc
				return type;				
			}
			else {
				// NULLABLE => add "?" after the csharp type
				if ( ! StrUtil.nullOrVoid(type) ) {
					return type + "?" ;
				}
				else {
					return type;
				}
			}
		}
		else {	
			throw new IllegalArgumentException("$csharp.nullableType(attribute) : attribute is null");
		}
	}
	
	//-------------------------------------------------------------------------------------
	// toString METHOD GENERATION
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns a string containing all the code for a C# 'ToString()' method",
			"Generates a 'ToString' method using all the attributes of the given entity",
			"(except non-printable attributes)",
			"Indentation with tabs (1 tab for each indentation level)"
			},
		example={ 
			"$csharp.toStringMethod( $entity, 2 )" },
		parameters = { 
			"entity : the entity for which to generate the 'ToString' method",
			"indentationLevel : initial indentation level"},
		since = "4.1.0"
			)
	public String toStringMethod( EntityInContext entity, int indentationLevel ) {
		return buildToStringMethod( entity, entity.getAttributes(), indentationLevel, new LinesBuilder() ); 
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns a string containing all the code for a C# 'ToString()' method",
			"Generates a 'ToString' method using all the attributes of the given entity",
			"(except non-printable attributes)",
			"Indentation with spaces (1 'indentationString' for each indentation level)"
			},
		example={ 
			"$csharp.toStringMethod( $entity, 2, '  ' )" },
		parameters = { 
			"entity : the entity for which to generate the 'ToString' method",
			"indentationLevel : initial indentation level",
			"indentationString : string to use for each indentation (usually N spaces)"},
		since = "4.1.0"
			)
	public String toStringMethod( EntityInContext entity, int indentationLevel, String indentationString ) {
		return buildToStringMethod( entity, entity.getAttributes(), indentationLevel, new LinesBuilder(indentationString) ); 
	}
        
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns a string containing all the code for a C# 'ToString()' method",
			"Generates a 'ToString' method using the given attributes ",
			"(except non-printable attributes)",
			"Indent with tabs (1 tab for each indentation level)"
			},
		example={ 
			"$csharp.toStringMethod( $entity, $attributes, 2 )" },
		parameters = { 
			"entity : the entity for which to generate the 'ToString' method",
			"attributes : list of attributes to be used in the 'ToString' method",
			"indentationLevel : initial indentation level"},
		since = "4.1.0"
			)
	public String toStringMethod( EntityInContext entity, List<AttributeInContext> attributes, int indentationLevel ) {
		return buildToStringMethod( entity, attributes, indentationLevel, new LinesBuilder() ); 
	}
    
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns a string containing all the code for a C# 'ToString()' method",
			"Generates a 'ToString' method using the given attributes ",
			"(except non-printable attributes)",
			"Indentation with spaces (1 'indentationString' for each indentation level)"
			},
		example={ 
			"$csharp.toStringMethod( $entity, $attributes, 2, '  ' )" },
		parameters = { 
			"entity : the entity for which to generate the 'ToString' method",
			"attributes : list of attributes to be used in the 'ToString' method",
			"indentationLevel : initial indentation level",
			"indentationString : string to use for each indentation (usually N spaces) "},
		since = "4.1.0"
			)
	public String toStringMethod( EntityInContext entity, List<AttributeInContext> attributes, int indentationLevel, String indentationString ) {
		return buildToStringMethod( entity, attributes, indentationLevel, new LinesBuilder(indentationString) ); 
	}
    
	//-------------------------------------------------------------------------------------
	/**
	 * Builds the string to be returned using the given attributes and the LinesBuilder
	 * @param entity
	 * @param attributes
	 * @param indentLevel
	 * @param lb
	 * @return
	 */
	private String buildToStringMethod( EntityInContext entity, List<AttributeInContext> attributes, int indentLevel, LinesBuilder lb ) {
    	if ( entity == null ) {
    		throw new IllegalArgumentException("$csharp.toStringMethod(..) : entity arg is null");
    	}
    	if ( attributes == null ) {
    		throw new IllegalArgumentException("$csharp.toStringMethod(..) : attributes arg is null");
    	}
		int indent = indentLevel ;
		lb.append(indent, "public override string ToString()");
		lb.append(indent, "{");
		indent++;
    	if ( attributes.isEmpty() ) {
    		//--- No attributes
    		lb.append(indent, "return \"" + entity.getName() + " [no attribute]\" ;");
    	}
    	else {
    		//--- Build return concat with all the given attributes 
    		buildToStringMethodWithStringBuilder( entity, attributes, indent, lb );
    	}
		indent--;
		lb.append(indent, "}");
		return lb.toString();
	}
    
	//-------------------------------------------------------------------------------------
    
    /**
     * Builds the string to be returned using the given attributes
     * @param entity
     * @param attributes
     * @param lb
     * @param indentationLevel
     */
    private void buildToStringMethodWithStringBuilder( EntityInContext entity, List<AttributeInContext> attributes, int indentationLevel, LinesBuilder lb) 
    {    	
    	if ( null == attributes ) return ;
    	int count = 0 ;
    	// first lines
		lb.append(indentationLevel, "System.Text.StringBuilder sb = new System.Text.StringBuilder();"); 
		lb.append(indentationLevel, "sb.Append(\"" + entity.getName() + "[\");");  // append the class name, example : sb.Append("Employee[")
    	for ( AttributeInContext attribute : attributes ) {
    		if ( usableInToString( attribute ) ) {
                if ( count > 0 ) {
                	lb.append(indentationLevel, "sb.Append(\"|\");"); // not the first one => append separator before
                }
    			lb.append(indentationLevel, "sb.Append(\"" + attribute.getName() + "=\").Append(" + attribute.getName() + ");"); 
    			// example: sb.Append("firstName=").Append(firstName) 
    			count++ ;
    		}
    		else {
    			lb.append(indentationLevel, "// attribute '" + attribute.getName() + "' (type " + attribute.getType() + ") not usable in ToString() " );
    		}
    	}
    	// last line
    	lb.append(indentationLevel, "sb.Append(\"]\");" ); 
		lb.append(indentationLevel, "return sb.ToString();" );
    }
    
    /**
     * Returns true if the given type is usable in a 'toString' method
     * @param sType
     * @return
     */
    private boolean usableInToString( AttributeInContext attribute ) {
    	return ! attribute.isBinaryType() && ! attribute.isLongText() ;
    }
	
}
