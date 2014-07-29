/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.isti.thematrix.scripting;

/**
 *
 * @author edoardovacchi
 */
public class AbstractModule {
    protected String name;
    public AbstractModule(String name) {
        setName(name);
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }  
}
