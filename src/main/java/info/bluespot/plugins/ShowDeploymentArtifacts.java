/**
 * Build-Info Maven Plugin.
 */
package info.bluespot.plugins;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This goal gathers all uploaded artifacts after a build and store them in a file (CSV, JSON or XML) for scripting use.
 */
@Mojo( name = "deployed-artifacts", requiresProject = true, threadSafe = true, inheritByDefault = true )
public class ShowDeploymentArtifacts
    extends AbstractMojo
{
    /**
     * Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject artifact;

    /**
     * Reactor projects.
     */
    @Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    private List<MavenProject> reactorProjects;

    /**
     * Maven session for variable resolution.
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    /**
     * Output filename.
     */
    @Parameter( defaultValue = "artifacts.csv", property = "outputFile", required = false, readonly = true )
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
     * Artifact list.
     */
    private ArrayList<MavenUploadedArtifact> artifacts = null;

    /**
     * Mojo main method.
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Check for Reactor projects
        if ( reactorProjects.isEmpty() )
        {
            System.out.println( "There isn't any project in the Reactor." );
            return;
        }

        // Run just in the last Reactor project (at the end of the Maven execution/artifact list).
        if ( reactorProjects.get( reactorProjects.size() - 1 ).equals( artifact ) )
        {
            // Gather all project information
            processAllProjects();

            // File format
            String format = outputFormat;

            if ( format == null )
            {
                format = Common.getOutputFormat( this.outputFile );
            }

            // Check for a empty artifact list
            if ( this.artifacts.isEmpty() )
            {
                System.out.println( "There aren't any uploaded artifacts." );
                return;
            }
            else
            {
                System.out.println( "Writting uploaded artifacts to file '" + outputFile + "' (format '" + format
                    + "')." );
            }

            // Output content
            ArrayList<String> output = null;

            if ( Common.OUTPUT_SH_EXPORT.equalsIgnoreCase( format ) )
            {
                output = toShExport();
            }
            else if ( Common.OUTPUT_JSON.equalsIgnoreCase( format ) )
            {
                try
                {
                    output = toJSON();
                }
                catch ( JsonProcessingException e )
                {
                    throw new MojoExecutionException( e.getMessage() );
                }
            }
            else if ( Common.OUTPUT_CSV.equalsIgnoreCase( format ) )
            {
                output = toCSV();
            }
            else if ( Common.OUTPUT_XML.equalsIgnoreCase( format ) )
            {
                output = toXML();
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
     * Process all projects looking for artifacts.
     */
    private void processAllProjects()
    {
        this.artifacts = new ArrayList<MavenUploadedArtifact>();

        for ( MavenProject project : reactorProjects )
        {
            // Adds the current POM artifact
            addUploadedArtifact( project, project.getArtifact() );

            // And then, the attached artifacts
            List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

            if ( !attachedArtifacts.isEmpty() )
            {
                for ( Artifact artifact : attachedArtifacts )
                {
                    addUploadedArtifact( project, artifact );
                }
            }
        }
    }

    /**
     * Vuelva información de un artefacto en STDOUT.
     */
    private void addUploadedArtifact( MavenProject project, Artifact artifact )
    {
        // Si hay información de la distribución, buscamos los artefactos añadidos
        if ( this.artifact.getDistributionManagementArtifactRepository() == null )
        {
            return;
        }

        String artifactUrl =
            this.artifact.getDistributionManagementArtifactRepository().getUrl() + getLocation( artifact, true );

        // Añadimos el artefacto a la lista
        MavenUploadedArtifact uploadedArtifact =
            new MavenUploadedArtifact( artifact.getGroupId(), artifact.getArtifactId(), project.getVersion(),
                                       artifact.getClassifier(), artifact.getType(), artifactUrl );
        artifacts.add( uploadedArtifact );
    }

    /**
     * 'sh-export' output.
     */
    private ArrayList<String> toShExport()
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "declare -a artifacts=(" );
        for ( MavenUploadedArtifact artifact : this.artifacts )
        {
            output.add( "'" + artifact.getArtifactId() + "'" );
            output.add( "    <version>" + artifact.getVersion() + "'" );
            output.add( "    <classifier>" + artifact.getClassifier() + "'" );
            output.add( "    <type>" + artifact.getType() + "'" );
            output.add( "    <url>" + artifact.getUrl() + "'" );
        }
        output.add( ")" );

        return output;
    }

    /**
     * XML output.
     */
    private ArrayList<String> toXML()
    {
        ArrayList<String> output = new ArrayList<String>();

        output.add( "<artifacts>" );

        for ( MavenUploadedArtifact artifact : this.artifacts )
        {
            output.add( "  <artifact>" );
            output.add( "    <artifactId>" + artifact.getArtifactId() + "</artifactId>" );
            output.add( "    <groupId>" + artifact.getGroupId() + "</groupId>" );
            output.add( "    <version>" + artifact.getVersion() + "</version>" );
            output.add( "    <classifier>" + artifact.getClassifier() + "</classifier>" );
            output.add( "    <type>" + artifact.getType() + "</type>" );
            output.add( "    <url>" + artifact.getUrl() + "</url>" );
            output.add( "  <artifact>" );

        }
        output.add( "</artifacts>" );

        return output;
    }

    /**
     * JSON output.
     * 
     * @throws JsonProcessingException Error processing the JSON output.
     */
    private ArrayList<String> toJSON()
        throws JsonProcessingException
    {
        ArrayList<String> output = new ArrayList<String>();

        ObjectMapper jsonMapper = new ObjectMapper();
        output.add( jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString( artifacts ) );

        return output;
    }

    /**
     * CSV output
     */
    private ArrayList<String> toCSV()
    {
        ArrayList<String> output = new ArrayList<String>();

        StringBuffer sb = new StringBuffer( "#groupId" );
        sb.append( csvSeparator );
        sb.append( "artifactId" );
        sb.append( csvSeparator );
        sb.append( "version" );
        sb.append( csvSeparator );
        sb.append( "type" );
        sb.append( csvSeparator );
        sb.append( "classifier" );
        sb.append( csvSeparator );
        sb.append( "url" );

        output.add( sb.toString() );

        for ( MavenUploadedArtifact artifact : this.artifacts )
        {
            output.add( artifactToCSV( artifact ) );
        }

        return output;
    }

    /**
     * Returns the CSV line for an artifact.
     */
    private String artifactToCSV( MavenUploadedArtifact artifact )
    {
        StringBuffer sb = new StringBuffer( artifact.getGroupId() );
        sb.append( csvSeparator );
        sb.append( artifact.getArtifactId() );
        sb.append( csvSeparator );
        sb.append( artifact.getVersion() );
        sb.append( csvSeparator );
        sb.append( artifact.getType() );
        sb.append( csvSeparator );
        sb.append( artifact.getClassifier() );
        sb.append( csvSeparator );
        sb.append( artifact.getUrl() );

        return ( sb.toString() );
    }

    /**
     * Gets the artifact URL. Code "kindly" taken from the "Maven2RepositoryLayoutFactory.java" (aether-core).
     */
    public URI getLocation( Artifact artifact, boolean upload )
    {
        StringBuilder path = new StringBuilder( 128 );

        path.append( '/' );

        path.append( artifact.getGroupId().replace( '.', '/' ) ).append( '/' );

        path.append( artifact.getArtifactId() ).append( '/' );

        path.append( artifact.getBaseVersion() ).append( '/' );

        // 'artifact.getBaseVersion()' - returns '0.1.2-SNAPSHOT'
        // 'artifact.getVersion()' - replaces the 'SNAPSHOT' for a timestamp
        path.append( artifact.getArtifactId() ).append( '-' ).append( artifact.getVersion() );

        if ( ( artifact.getClassifier() != null ) && ( artifact.getClassifier().length() > 0 ) )
        {
            path.append( '-' ).append( artifact.getClassifier() );
        }

        if ( ( artifact.getType() != null ) )
        {
            // See: maven-core/src/site/apt/artifact-handlers.apt
            if ( artifact.getType().equals( "java-source" ) || artifact.getType().equals( "maven-plugin" )
                || artifact.getType().equals( "ejb" ) || artifact.getType().equals( "javadoc" )
                || artifact.getType().equals( "ejb-client" ) || artifact.getType().equals( "test-jar" ) )
            {
                path.append( ".jar" );
            }
            else
            {
                path.append( '.' ).append( artifact.getType() );
            }
        }

        return toUri( path.toString() );
    }

    /**
     * 'toUri' (aether-core).
     */
    private URI toUri( String path )
    {
        try
        {
            return new URI( null, null, path, null );
        }
        catch ( URISyntaxException e )
        {
            throw new IllegalStateException( e );
        }
    }

}
