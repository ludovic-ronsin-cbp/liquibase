package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.View;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import liquibase.exception.JDBCException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class CreateViewStatementTest extends AbstractSqlStatementTest {

    private static final String VIEW_NAME = "CreateViewTest";
    private static final String TABLE_NAME = "CreateViewTestTable";

    protected SqlStatement generateTestStatement() {
        return new CreateViewStatement(null, null, null);
    }

    protected void setupDatabase(Database database) throws Exception {

        dropViewIfExists(null, VIEW_NAME, database);

        dropViewIfExists(TestContext.ALT_SCHEMA, VIEW_NAME, database);

        dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                .addPrimaryKeyColumn("id", "int")
                .addColumn("name", "varchar(50)")
                , database);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        final String definition = "SELECT * FROM " + TABLE_NAME;

        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new CreateViewStatement(null, VIEW_NAME, definition)) {
                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        View view = snapshot.getView(VIEW_NAME);
                        assertNotNull(view);
                        assertEquals(2, view.getColumns().size());
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        final String definition = "SELECT * FROM " + TABLE_NAME;
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, definition)) {
                    protected boolean supportsTest(Database database) {
                        return !(database instanceof HsqlDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getView(VIEW_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        View view = snapshot.getView(VIEW_NAME);
                        assertNotNull(view);
                        assertEquals(2, view.getColumns().size());
                    }

                });
    }
}
