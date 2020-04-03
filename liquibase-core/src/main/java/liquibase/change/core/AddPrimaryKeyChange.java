package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.DB2Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
@DatabaseChange(name="addPrimaryKey", description = "Adds a primary key out of an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddPrimaryKeyChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;
    private Boolean clustered;
    private String forIndexName;
    private String forIndexSchemaName;
    private String forIndexCatalogName;
    private Boolean shouldValidate;

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table to create the primary key on")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column(s) to create the primary key on. Comma separated if multiple")
    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    @DatabaseChangeProperty(description = "Name of primary key constraint", exampleValue = "pk_person")
    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getForIndexName() {
        return forIndexName;
    }

    public void setForIndexName(String forIndexName) {
        this.forIndexName = forIndexName;
    }

    public String getForIndexSchemaName() {
        return forIndexSchemaName;
    }

    public void setForIndexSchemaName(String forIndexSchemaName) {
        this.forIndexSchemaName = forIndexSchemaName;
    }

    public String getForIndexCatalogName() {
        return forIndexCatalogName;
    }

    public void setForIndexCatalogName(String forIndexCatalogName) {
        this.forIndexCatalogName = forIndexCatalogName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public Boolean getClustered() {
        return clustered;
    }

    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * the VALIDATE keyword defines whether a primary key constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    @DatabaseChangeProperty(description = "This is true if the primary key has 'ENABLE VALIDATE' set, or false if the primary key has 'ENABLE NOVALIDATE' set.")
    public Boolean getValidate() {
        return shouldValidate;
    }

    /**
     *
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid primary keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for primary keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public void setValidate(Boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        boolean shouldValidate = true;
        if (getValidate() != null) {
            shouldValidate = getValidate();
        }
        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());
        statement.setClustered(getClustered());
        statement.setForIndexName(getForIndexName());
        statement.setForIndexSchemaName(getForIndexSchemaName());
        statement.setForIndexCatalogName(getForIndexCatalogName());
        statement.setShouldValidate(shouldValidate);

        if (database instanceof AbstractDb2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName())
            };
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            PrimaryKey example = new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(), getTableName(), Column.arrayFromNames(getColumnNames()));

            PrimaryKey snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Primary key does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    protected Change[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        if (this.getForIndexName() != null) {
            inverse.setDropIndex(false);
        }

        return new Change[]{
                inverse,
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key added to " + getTableName() + " (" + getColumnNames() + ")";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
