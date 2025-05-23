package org.telosys.tools.generator;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.telosys.tools.commons.bundles.TargetDefinition;
import org.telosys.tools.commons.cfg.TelosysToolsCfg;
import org.telosys.tools.generator.context.Target;
import org.telosys.tools.generic.model.Entity;
import org.telosys.tools.generic.model.Model;

import junit.env.telosys.tools.generator.FakeProject;
import junit.env.telosys.tools.generator.fakemodel.FakeModelProvider;
import junit.env.telosys.tools.generator.fakemodel.entities.Employee;


public class ProjectUTF8Test {

	private FakeProject fakeProject = new FakeProject("proj-utf8");
	
	private Target getTarget(String templateFile, String generatedFile, Entity entity) {
		TelosysToolsCfg telosysToolsCfg = fakeProject.getTelosysToolsCfg();		
		TargetDefinition targetDefinition = new TargetDefinition(
				"Fake target", 
				generatedFile, // "utf8.txt", 
				"generated-files", 
				templateFile, //"utf8_txt.vm", 
				"*");
		return new Target( telosysToolsCfg.getDestinationFolderAbsolutePath(), targetDefinition, telosysToolsCfg.getAllVariablesMap(), entity );  // v 4.2.0
	}
	
	private List<String> getSelectedEntities() {
		List<String> list = new LinkedList<>();
		list.add("Author");
		return list;
	}
	
	private void launchGeneration(String templateFile, String generatedFile) throws GeneratorException {
		Generator generator = fakeProject.getGenerator("bundle-utf8") ;
		Model model = FakeModelProvider.buildModel();
		Entity entity = model.getEntityByClassName(Employee.ENTITY_NAME);
		Target target = getTarget(templateFile, generatedFile, entity);
		List<String> selectedEntitiesNames = getSelectedEntities();
		generator.generateTarget(target, model, selectedEntitiesNames, null);
	}
	
	@Test
	public void testUtf8Txt() throws GeneratorException {
		launchGeneration("utf8_txt.vm", "utf8.txt");
	}

	@Test
	public void testOpenapiYaml() throws GeneratorException {
		launchGeneration("openapi_yaml.vm", "openapi.yaml");
	}

	@Test
	public void testOpenapiYamlTxt() throws GeneratorException {
		launchGeneration("openapi_yaml.vm", "openapi_yaml.txt");
	}
}
