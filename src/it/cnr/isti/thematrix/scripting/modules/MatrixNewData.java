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
import java.util.List;

/**
 * Creates a new dataset with the given schema.
 * TODO  complete the  implementation
 * @author edoardovacchi
 */
public class MatrixNewData extends MatrixModule {
    private final List<Symbol<?>> schema;

    public MatrixNewData(String name, List<Symbol<?>> schema) { 
        super(name); 
        this.schema = schema;
    }
    
    @Override
    public void setup() {
        DatasetSchema customSchema = new DatasetSchema(this.name+"$custom");
        customSchema.putAll(schema);
        this.setSchema(customSchema);
        LogST.logP(1,"MatrixNewData.setup() done. " +this.toString());
    }

	@Override
	public void exec() {
		LogST.logP(2,"NO-OP EXEC "+this.name);
	}

    public String toString() {
        return String.format( 
        "NewDataModule named '%s'\n with schema:\n  %s",
        name,
        schema); 
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
