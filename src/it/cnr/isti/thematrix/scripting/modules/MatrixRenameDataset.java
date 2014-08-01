/*
 * Copyright (c) 2010-2014 "HPCLab at ISTI-CNR"
 *
 * This file is part of TheMatrix.
 *
 * TheMatrix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.isti.thematrix.scripting.modules;

import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.List;

/**
 *
 * @author edoardovacchi
 */
public class MatrixRenameDataset extends MatrixModule {
    

    private final MatrixModule inputModule;
    public MatrixRenameDataset(String name, String inputModule, String schema) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputModule);
        this.inputModule.schemaMatches(schema);
        this.inputModule.addConsumer(this);
    }
    
    public String toString() {
        return String.format(
           "MatrixRenameDataset named '%s'\n with parameters:\n  %s \n\n",
            name,
         inputModule.name
         
       );
    }

    @Override
    public void setup() {
        
        this.setSchema(inputModule.getSchema());
    }
    

	@Override
	public void exec() {
		inputModule.exec(); // FIXME seems like a bug where exec() is called twice
	}


    @Override
    public void reset() {
        inputModule.reset();
    }

    @Override
    public boolean hasMore() {
        return inputModule.hasMore();
    }

    @Override
    public void next() {
        this.inputModule.next();
    }
    
    @Override
    public Symbol<?> get(Object o) {
    	return this.inputModule.get(o);
    }
    
    @Override
    public List<Symbol<?>> attributes() {
    	return this.inputModule.attributes();
    }
    
}
