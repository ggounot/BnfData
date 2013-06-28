package eu.gounot.bnfdata.database;

public interface DatabaseInstallationListener {

    void onDatabaseInstallationProgressUpdate(int progress);

    void onDatabaseInstallationComplete();
}
