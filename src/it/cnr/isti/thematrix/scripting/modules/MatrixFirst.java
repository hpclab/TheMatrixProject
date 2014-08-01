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

import it.cnr.isti.thematrix.configuration.LogST;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;
import java.util.List;

/**
 *
 * Equivalent to UNIX `uniq` tool. 
 * 
 * TODO Complete the implementation.
 * 
 * @author edoardovacchi
 */
public class MatrixFirst extends MatrixModule {
    private final MatrixModule inputModule;
    private final List<String> fieldNames;
    
    public MatrixFirst(
            String name, 
            String inputTable, 
            String schemaName, 
            List<String> fieldNames) {
        super(name);
        this.inputModule = TheMatrixSys.getModule(inputTable);
        inputModule.schemaMatches(schemaName);
        this.inputModule.addConsumer(this);

        this.fieldNames = fieldNames;
        
    }
    
    @Override
    public void setup() {
        this.setSchema(inputModule.getSchema());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    
    public String toString() {
        return String.format(
           "FirstModule named '%s'\n with parameters:\n  %s\n  %s\n\n",
            name,
         inputModule.name, fieldNames
         
       );
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasMore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
