/**
 * Build-Info Maven Plugin.
 */
package info.bluespot.plugins;

import java.util.ArrayList;

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Save the 'distributionManagement' (snapshots and releases repositories) of the current project in a
 * CSV/JSON/XML/Shell file.
 */
@Mojo( name = "distribution-management", requiresProject = true, threadSafe = true, inheritByDefault = false )
public class GetDistributionManagement
    extends AbstractMojo
{
    /**
     * Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    /**
     * Output filename.
     */
    @Parameter( defaultValue = "distribution-management.csv", property = "outputFile", required = false, readonly = true )
    private String outputFile;

    /**
     * Output format: "csv", "sh", "json" or "xml". Default value calculated from the <code>outputFile</code> extension.
     */
    @Parameter( property = "outputFormat", required = false, readonly = true )
    private String outputFormat;

    /**
     * CSV separator character.
     */
    @Parameter( defaultValue = ",", property = "csvSeparator", required = false, readonly = true )
    private String csvSeparator;

    /**
     * Mojo's main method.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Get the model
        Model model = project.getModel();
        DistributionManagement dm = model.getDistributionManagement();

        if ( dm != null )
        {
            // Output content
            ArrayList<String> output = null;

            // File format
            String format = this.outputFormat;
            if ( format == null )
            {
                format = Common.getOutputFormat( this.outputFile );
            }

            if ( Common.OUTPUT_SH_EXPORT.equalsIgnoreCase( format ) )
            {
                output = toShExport( dm );
            }
            else if ( Common.OUTPUT_JSON.equalsIgnoreCase( format ) )
            {
                output = toJSON( dm );
            }
            else if ( Common.OUTPUT_CSV.equalsIgnoreCase( format ) )
            {
                output = toCSV( dm );
            }
            else if ( Common.OUTPUT_XML.equalsIgnoreCase( format ) )
            {
                output = toXML( dm );
            }
            else
            {
                throw new MojoFailureException( "Invalid output format: '" + format + "'" );
            }

            // Write the output
            System.out.println( "Writting distribution management information to file '" + outputFile + "' (format '"
                + format + "')." );
            Common.writeToFile( output, this.outputFile );
        }
    }

    /**
     * 'sh-export' output.
     */
    private ArrayList<String> toShExport( DistributionManagement dm )
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "releases_id='" + dm.getRepository().getId() + "'" );
        output.add( "releases_name='" + dm.getRepository().getName() + "'" );
        output.add( "releases_url='" + dm.getRepository().getUrl() + "'" );
        output.add( "releases_layout='" + dm.getRepository().getLayout() + "'" );

        output.add( "snapshot_id='" + dm.getSnapshotRepository().getId() + "'" );
        output.add( "snapshot_name='" + dm.getSnapshotRepository().getName() + "'" );
        output.add( "snapshot_url='" + dm.getSnapshotRepository().getUrl() + "'" );
        output.add( "snapshot_layout='" + dm.getSnapshotRepository().getLayout() + "'" );

        return output;
    }

    /**
     * JSON output.
     */
    private ArrayList<String> toJSON( DistributionManagement dm )
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "{" );
        output.add( "\"releases_id\":\"" + dm.getRepository().getId() + "\"," );
        output.add( "\"releases_name\":\"" + dm.getRepository().getName() + "\"," );
        output.add( "\"releases_url\":\"" + dm.getRepository().getUrl() + "\"," );
        output.add( "\"releases_layout\":\"" + dm.getRepository().getLayout() + "\"," );

        output.add( "\"snapshot_id\":\"" + dm.getSnapshotRepository().getId() + "\"," );
        output.add( "\"snapshot_name\":\"" + dm.getSnapshotRepository().getName() + "\"," );
        output.add( "\"snapshot_url\":\"" + dm.getSnapshotRepository().getUrl() + "\"," );
        output.add( "\"snapshot_layout\":\"" + dm.getSnapshotRepository().getLayout() + "\"" );
        output.add( "}" );

        return output;
    }

    /**
     * XML output.
     */
    private ArrayList<String> toXML( DistributionManagement dm )
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "<distributionManagement>" );
        output.add( "  <releasesId>" + dm.getRepository().getId() + "</releasesId>" );
        output.add( "  <releasesName>" + dm.getRepository().getName() + "</releasesName>" );
        output.add( "  <releasesUrl>" + dm.getRepository().getUrl() + "</releasesUrl>" );
        output.add( "  <releasesLayout>" + dm.getRepository().getLayout() + "</releasesLayout>" );

        output.add( "  <snapshotId>" + dm.getSnapshotRepository().getId() + "</snapshotId>" );
        output.add( "  <snapshotName>" + dm.getSnapshotRepository().getName() + "</snapshotName>" );
        output.add( "  <snapshotUrl>" + dm.getSnapshotRepository().getUrl() + "</snapshotUrl>" );
        output.add( "  <snapshotLayout>" + dm.getSnapshotRepository().getLayout() + "</snapshotLayout>" );
        output.add( "</distributionManagement>" );

        return output;
    }

    /**
     * CSV output
     */
    private ArrayList<String> toCSV( DistributionManagement dm )
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "#key" + csvSeparator + "value" );
        output.add( "releases_id" + csvSeparator + dm.getRepository().getId() );
        output.add( "releases_name" + csvSeparator + dm.getRepository().getName() );
        output.add( "releases_url" + csvSeparator + dm.getRepository().getUrl() );
        output.add( "releases_layout" + csvSeparator + dm.getRepository().getLayout() );

        output.add( "snapshot_id" + csvSeparator + dm.getSnapshotRepository().getId() );
        output.add( "snapshot_name" + csvSeparator + dm.getSnapshotRepository().getName() );
        output.add( "snapshot_url" + csvSeparator + dm.getSnapshotRepository().getUrl() );
        output.add( "snapshot_layout" + csvSeparator + dm.getSnapshotRepository().getLayout() );

        return output;
    }
}
