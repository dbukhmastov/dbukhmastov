<LST Generator>
1. Include following in the ant build script:
<!-- Load external ant extension for SQA team -->
<taskdef resource="sqe/antext/defs.properties">
	<classpath>
		<pathelement path="../lib/AntExt.jar"/>
	</classpath>
</taskdef>	

2. Add following to the build.xml(this will be added to the last line of the 'compileTests' target, see TestTemplate's build.xml):
<!-- generate testClasses.lst -->
<sqa-lstgenerator srcpath="${tests.dir}" classpath="${testsuite.dir}/${classes.dir}" dependencypackages="${dependency.packages}"/>

3. Review 'dependency.packages' value from 'build.properties':
dependency.packages=sqe/tests/template

'dependency.packages' is a comma separated list of package prefixes for dependencies(for example, 'sqe/tests,aPackage,AnotherPackage').

4. Then the testClasses.lst will be generated at '<testpack_home>/classes/shared/testClasses.lst' while building testpack.