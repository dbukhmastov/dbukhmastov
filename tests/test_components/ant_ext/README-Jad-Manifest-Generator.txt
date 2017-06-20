<JadGenerator and ManifestGenerator>
1. Include following in the ant build script:
<!-- Load external ant extension for SQA team -->
<taskdef resource="sqe/antext/defs.properties">
	<classpath>
		<pathelement path="../lib/AntExt.jar"/>
	</classpath>
</taskdef>	
	
2-1. Use 'sqa-manifestgenerator' for generating Manifest file. Available options are 'propertyfile', 'globalpropertyfile' and 'destdir'.
<target name="create-manifest" depends="compile" description="Create a manifest">
	<sqa-manifestgenerator propertyfile="./MIDlets/${project.name}.properties" globalpropertyfile="./build.properties" destdir="${temp}" />
</target>

2-2. Use 'sqa-jadgenerator' for generating Jad file. Available options are 'propertyfile', 'globalpropertyfile', 'destdir' and 'jarfile'.
<target name="create-jad" depends="compile" description="Create a manifest">
	<sqa-jadgenerator propertyfile="./MIDlets/${project.name}.properties" globalpropertyfile="./build.properties" jarfile="${midlet.dist.dir}/${jarfile.name}.jar" destdir="${midlet.dist.dir}" />
</target>

3. Prepare the 'globalpropertyfile'(optional) and 'propertyfile'(mandatory). Here is the example of the files:
<build.properties> - globalpropertyfile
sdk.home=D:/appl/ME8SDK/promoted/b32
j2me.classpath=${sdk.home}/runtimes/meep/lib/classes.zip
api.classpath=${sdk.home}/runtimes/meep/lib/classes.zip
midlet.api.classpath=${api.classpath}
preverify.api.classpath=${api.classpath}

prov.host=http://berlin.ru.oracle.com:8090/ME8/sqeTest/imc
......

<${project.name>.properties> : property file's name should be defined as 'project.name' in batch file. See buildMIDlets.bat as an example. All attributes should start with J- or M- or JM- which means add to Jad or Manifest or both Jad and Manifest respectively.
JM-MIDlet-1: IMCClient,,sqe.tests.imc.util.IMCClient
JM-MIDlet-Jar-URL: IMCClient.jar
JM-MIDlet-Name: IMCClient
JM-MIDlet-Vendor: Oracle
JM-MIDlet-Version: 1.0
JM-MicroEdition-Configuration: CLDC-1.8
JM-MicroEdition-Profile: MEEP-8.0
JM-ProvHost: ${prov.host}

4. Manifest and jad will be created then buildMIDlets.xml will generate final jad/jar.
<IMCClient.jad>
MIDlet-Version: 1.0
MIDlet-Jar-Size: 10847
MicroEdition-Configuration: CLDC-1.8
MIDlet-Jar-URL: IMCClient.jar
MIDlet-Name: IMCClient
MIDlet-1: IMCClient,,sqe.tests.imc.util.IMCClient
ProvHost: http://berlin.ru.oracle.com:8090/ME8/sqeTest/imc <-- ${prov.host} is resolved.
MicroEdition-Profile: MEEP-8.0
MIDlet-Vendor: Oracle
MIDlet-Certificate-1-1: MIICgTCCAeqgAwIBAgIEUJEx3jANBgkqhkiG9w0BAQUFADCBhDEVMBMGA1UEBhMMbWFudWZhY3R1cmVyMRUwEwYDVQQIEwxtYW51ZmFjdHVyZXIxFTATBgNVBAcTDG1hbnVmYWN0dXJlcjEVMBMGA1UEChMMbWFudWZhY3R1cmVyMRUwEwYDVQQLEwxtYW51ZmFjdHVyZXIxDzANBgNVBAMTBmJhcnN1azAeFw0xMjEwMzExNDEyNDZaFw0yMjA5MDkxNDEyNDZaMIGEMRUwEwYDVQQGEwxtYW51ZmFjdHVyZXIxFTATBgNVBAgTDG1hbnVmYWN0dXJlcjEVMBMGA1UEBxMMbWFudWZhY3R1cmVyMRUwEwYDVQQKEwxtYW51ZmFjdHVyZXIxFTATBgNVBAsTDG1hbnVmYWN0dXJlcjEPMA0GA1UEAxMGYmFyc3VrMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQZzTIDNNXh1LhncPI70ujsWLV/iSH6/Ei/D1cjuw4EGyAo6la/JQGoR2hHNUGMh35RkLod8KTQrxSzWNIfl9nkO4cvG9ASuEbl+1APqvkul/XdpgxLPvA1SMyN5BZkzQ9+3vL3iCr8wQ76uD3DVMCcgYIUuLhL+bXLWe1KtAx0QIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAHdiwrjrTB8IgR6a5nGBS/Pam22QpDcm4OXWCTHyGKk8Zxs5swsa0EP+/E13JKX3RUN74/ymTPQelMH4BixdIBcmFzfJJF56Adf4RmGdmz1po7ixhJiilhqSiUGJNGBm8EcnrWm8OdtfpFlc0hcBELX0s0lSQ/C4869q6R8GCzKW
MIDlet-Jar-RSA-SHA1-1: QT+m92MlcQj4aQ5wpbf958LFhI3h5kVDbrG3Jy9sY42c7W4LBAKPpmfRUtv3AQsZt/aabvIt+8LMeQLIky1aNtwo0lh+4IfEuciWGC4WPujNPJVtcUXn5jtJZxx2dN46LxIE5chxFZ8NBoZ2zQpY8UtVxZ8t0v1+aKl1OSrcjmg=
