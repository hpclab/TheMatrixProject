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
import it.cnr.isti.thematrix.scripting.sys.DatasetSchema;
import it.cnr.isti.thematrix.scripting.sys.MatrixModule;
import it.cnr.isti.thematrix.scripting.sys.Symbol;
import it.cnr.isti.thematrix.scripting.sys.TheMatrixSys;

/**
 *
 * @author edoardovacchi
 */
public class MatrixProjection extends MatrixModule {
    
    MatrixModule input1, input2;
    String joinColumn1, joinColumn2, inputColumn, resultColumn;

    public MatrixProjection(
            String name, 
            String input1, 
            String schema1, 
            String input2, 
            String schema2, 
            String joinColumn1, 
            String joinColumn2, 
            String inputColumn, 
            String resultColumn) {
        super(name);
        this.input1 = TheMatrixSys.getModule(input1);
        this.input2 = TheMatrixSys.getModule(input2);

        this.joinColumn1 = joinColumn1;
        this.joinColumn2 = joinColumn2;
        this.inputColumn = inputColumn;
        this.resultColumn = resultColumn;
        
        this.input1.schemaMatches(schema1);
        this.input2.schemaMatches(schema2);

        this.input1.addConsumer(this);
        this.input2.addConsumer(this);

    }


    @Override
    public void setup() {
        Symbol<?> newCol = input2.getSchema().get(inputColumn).clone();
        newCol.setName(resultColumn);
        this.setSchema(DatasetSchema.extend(input1.getSchema(), newCol));
        LogST.logP(1,"MatrixProjection.setup() done. " +this.toString());
    }
    

	@Override
	public void exec() {
		LogST.logP(2,"EXEC "+this.name);
	}

    
    public String toString() {
        return String.format(
            "ProjectionModule named '%s'\nwith inputs:\n  [%s %s]\nand parameters\n"+
            "  join on %s = %s\n   copy %s to %s\n\n",

            name,
            input1.name, input2.name,
            joinColumn1, joinColumn2,
            inputColumn, resultColumn
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
