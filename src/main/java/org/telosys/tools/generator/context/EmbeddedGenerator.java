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

import org.telosys.tools.commons.TelosysToolsLogger;
import org.telosys.tools.commons.bundles.TargetDefinition;
import org.telosys.tools.commons.cfg.TelosysToolsCfg;
import org.telosys.tools.generator.Generator;
import org.telosys.tools.generator.GeneratorException;
import org.telosys.tools.generator.GeneratorVersion;
import org.telosys.tools.generator.TargetBuilder;
import org.telosys.tools.generator.context.doc.VelocityMethod;
import org.telosys.tools.generator.context.doc.VelocityObject;
import org.telosys.tools.generator.context.names.ContextName;
import org.telosys.tools.generic.model.Entity;
import org.telosys.tools.generic.model.Model;

/**
 * Embedded generator stored in the Velocity Context and usable in a template.
 * 
 * @author Laurent GUERIN
 *
 */
//-------------------------------------------------------------------------------------
@VelocityObject(
		contextName= ContextName.GENERATOR ,
		text = "Embedded generator usable in a template to generate another target ",
		since = "2.0.3"
 )
//-------------------------------------------------------------------------------------
public class EmbeddedGenerator {

	private final Model              model ;
	private final TelosysToolsCfg    telosysToolsCfg ; // v 3.0.0
	private final String             bundleName ; // v 3.0.0
	
	private final TelosysToolsLogger logger ;
	private final List<String>       selectedEntitiesNames;	
	private final boolean            canGenerate ;
	private final List<Target>       generatedTargets ;
	
	/**
	 * Constructor for limited embedded generator without generation capabilities
	 */
	public EmbeddedGenerator() {
		super();
		this.model = null ;
		this.telosysToolsCfg = null ; // v 3.0.0
		this.bundleName = null ; // v 3.0.0
		this.logger = null ;
		this.selectedEntitiesNames = null ;
		this.canGenerate = false ;
		this.generatedTargets = null ;
	}

	/**
	 * Constructor for real embedded generator that can generate sub-targets from a template
	 * @param telosysToolsCfg
	 * @param bundleName
	 * @param logger
	 * @param model
	 * @param selectedEntitiesNames
	 * @param generatedTargets
	 */
	public EmbeddedGenerator(	
			TelosysToolsCfg telosysToolsCfg, // v 3.0.0
			String bundleName, // v 3.0.0
			TelosysToolsLogger logger, 
			
			Model model, 
			List<String> selectedEntitiesNames,
			List<Target> generatedTargets) {
		super();
		// this.generatorConfig = generatorConfig; // v 3.0.0
		this.telosysToolsCfg = telosysToolsCfg ; // v 3.0.0
		this.bundleName = bundleName ; // v 3.0.0
		this.logger = logger;
		
		this.model = model;
		this.selectedEntitiesNames = selectedEntitiesNames ;
		this.generatedTargets = generatedTargets ;

		if ( this.model != null && this.telosysToolsCfg != null && this.bundleName != null && this.logger != null ) {
			this.canGenerate = true ;
		}
		else {
			this.canGenerate = false ;
		}
	}

	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the generator's name "
			},
		example = {
			"// Generated by $generator.name "
		}
	)
	public String getName()
	{
		return "Telosys embedded generator";
	}
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Returns the generator's version "
			},
		example = {
			"// Generator version : $generator.version "
		}
	)
	public String getVersion()
    {
        return GeneratorVersion.getVersion();
    }
	
	//-------------------------------------------------------------------------------------
	@VelocityMethod(
		text={	
			"Generates an other target with the given template file "
			},
		parameters = { 
			"entityClassName : the entity class name",
			"outputFile : the file name to be generated ",
			"outputFolder : the folder where to generate the file",			
			"templateFile : the template file to be used "			
			},
		example = {
			"#if ( $entity.hasCompositePrimaryKey() )",
			"$generator.generate($target.entityName , \"${entity.name}Key.java\", $target.folder, \"jpa_bean_pk.vm\" ) ",
			"#end"
		}
	)
	public void generate(String entityClassName, String outputFile, String outputFolder, String templateFile) throws GeneratorException
	{
		String err = "Error in embedded generator ";
		
		if ( ! canGenerate ) {
			throw new GeneratorException( err + "(embedded generator is not able to generate, environment is not available)");
		}
		if ( null == entityClassName ) {
			throw new GeneratorException( err + "(entity class name is null)");
		}
		if ( null == outputFile ) {
			throw new GeneratorException( err + "(output file is null)");
		}
		if ( null == outputFolder ) {
			throw new GeneratorException( err + "(output folder is null)");
		}
		if ( null == templateFile ) {
			throw new GeneratorException( err + "(template file is null)");
		}
		
		//--- Search the entity in the model
		Entity entity = model.getEntityByClassName(entityClassName.trim());
		if ( null == entity ) {
			throw new GeneratorException( err + "(entity '" + entityClassName + "' not found in repository)");
		}
		
		TargetDefinition targetDefinition = new TargetDefinition("Dynamic target", outputFile, outputFolder, templateFile, "");
		
		//Target target = new Target( telosysToolsCfg, targetDefinition, entity ); // v 3.3.0
		Target target = TargetBuilder.buildTarget(telosysToolsCfg, targetDefinition, bundleName, model, entity); // v 4.2.0
		
		Generator generator = new Generator(this.telosysToolsCfg, this.bundleName, logger);
		
		generator.generateTarget(target, model, selectedEntitiesNames, this.generatedTargets);
	}
	
}
