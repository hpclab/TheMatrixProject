package it.cnr.isti.thematrix.scripting;
import neverlang.runtime.Syntax;

public class FileOutputModule$role$syntax extends Syntax {
 public FileOutputModule$role$syntax() {
   declareProductions(
    p(nt("Module"), nt("ModuleId"), "(", "FileOutputModule", ")", "inputs", nt("ModuleImport"), "parameters", "checksum", "=", nt("FOChecksum"), "compression", "=", nt("FOCompressionType"), "end"),
    p(nt("FOChecksum"), "none"),
    p(nt("FOChecksum"), "md5"),
    p(nt("FOCompressionType"), "none"),
    p(nt("FOCompressionType"), "zip"),
    p(nt("FOCompressionType"), "gzip")
   );
 }
}