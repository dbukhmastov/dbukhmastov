/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */
package sqe.antext;


public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        System.out.println("hello");
//        WriteJarSize test = new WriteJarSize();
//        test.execute();
        
//        ManifestGenerator manifestGen = new ManifestGenerator();
//        manifestGen.setPropertyFile(new File("d:/temp/JadGenerator/MIDlets/FillRecordStore.properties"));
//        manifestGen.setGlobalPropertyFile(new File("d:/temp/JadGenerator/build.properties"));
//        manifestGen.setDestDir("d:/temp/JadGenerator");
//        manifestGen.setType("midlet");
//        manifestGen.execute();
        
//        JadGenerator jadGen = new JadGenerator();
//        jadGen.setPropertyFile(new File("d:/temp/JadGenerator/MIDlets/FillRecordStore.properties"));
//        jadGen.setGlobalPropertyFile(new File("d:/temp/JadGenerator/global.properties"));
//        jadGen.setDestDir("d:/temp/JadGenerator");
//        jadGen.setJarFile(new File("d:/temp/JadGenerator/IMCClient.jar"));
//        jadGen.setType("midlet");
//        jadGen.execute();
        
//        LSTGenerator gen = new LSTGenerator();
//        gen.setSrcPath("D:\\temp\\JadGenerator\\simplePrj\\tests");
//        gen.setClassPath("D:\\temp\\JadGenerator\\simplePrj\\classes");
//        gen.setDependencyPackages("sqe/tests/imc");
//        gen.execute();

        LSTGenerator gen = new LSTGenerator();
        gen.setSrcPath("D:\\temp\\JadGenerator\\complexPrj\\tests");
        gen.setClassPath("D:\\temp\\JadGenerator\\complexPrj\\classes");
        gen.setDependencyPackages("sqe,util");
        gen.execute();
    }
}
